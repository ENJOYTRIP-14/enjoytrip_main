package com.ssafy.trip.image;

import java.awt.image.BufferedImage;
import java.io.ByteArrayInputStream;
import java.io.BufferedInputStream;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.net.URLConnection;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.Base64;
import java.util.List;
import java.util.UUID;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import javax.imageio.ImageIO;

/** OpenAI Image Edit API를 호출해 메모리상의 이미지를 반환한다. */
public class OpenAiImageGenerationClient {

	private static final String API_URL = "https://api.openai.com/v1/images/edits";
	private static final String MODEL = "gpt-image-2";
	private static final Pattern IMAGE_DATA_PATTERN = Pattern.compile("\\\"b64_json\\\"\\s*:\\s*\\\"([^\\\"]+)\\\"");
	private static final Pattern ERROR_MESSAGE_PATTERN = Pattern.compile("\\\"message\\\"\\s*:\\s*\\\"([^\\\"]+)\\\"");

	public BufferedImage generate(File destinationImage, File userImage) throws IOException {
		return generate(destinationImage, userImage, createDefaultPrompt());
	}

	public BufferedImage generate(File destinationImage, File userImage, String prompt) throws IOException {
		validateImage(destinationImage, "관광지 이미지");
		validateImage(userImage, "사용자 이미지");
		if (prompt == null || prompt.trim().isEmpty()) {
			throw new IOException("이미지 생성 프롬프트가 비어 있습니다.");
		}

		String boundary = "----EnjoyTripBoundary" + UUID.randomUUID().toString().replace("-", "");
		HttpURLConnection connection = (HttpURLConnection) new URL(API_URL).openConnection();
		connection.setRequestMethod("POST");
		connection.setConnectTimeout(15_000);
		connection.setReadTimeout(120_000);
		connection.setDoOutput(true);
		connection.setRequestProperty("Authorization", "Bearer " + loadApiKey());
		connection.setRequestProperty("Content-Type", "multipart/form-data; boundary=" + boundary);

		try (OutputStream output = connection.getOutputStream()) {
			writeTextPart(output, boundary, "model", MODEL);
			writeTextPart(output, boundary, "prompt", prompt);
			writeTextPart(output, boundary, "quality", "low");
			writeTextPart(output, boundary, "output_format", "jpeg");
			writeTextPart(output, boundary, "output_compression", "85");
			writeFilePart(output, boundary, "image[]", destinationImage);
			writeFilePart(output, boundary, "image[]", userImage);
			writeUtf8(output, "--" + boundary + "--\r\n");
		}

		int status = connection.getResponseCode();
		String response = readResponse(
				status >= 200 && status < 300 ? connection.getInputStream() : connection.getErrorStream());
		if (status < 200 || status >= 300) {
			throw new IOException("이미지 생성 요청 실패 (HTTP " + status + "): " + extractErrorMessage(response));
		}

		Matcher matcher = IMAGE_DATA_PATTERN.matcher(response);
		if (!matcher.find()) {
			throw new IOException("OpenAI 응답에서 생성 이미지를 찾지 못했습니다.");
		}

		byte[] imageBytes = Base64.getDecoder().decode(matcher.group(1));
		BufferedImage generatedImage = ImageIO.read(new ByteArrayInputStream(imageBytes));
		if (generatedImage == null) {
			throw new IOException("생성 결과를 이미지로 변환하지 못했습니다.");
		}
		return generatedImage;
	}

	private String loadApiKey() throws IOException {
		List<String> lines = Files.readAllLines(Paths.get(".env"), StandardCharsets.UTF_8);
		for (String line : lines) {
			String trimmed = line.trim();
			if (trimmed.startsWith("OPENAI_API_KEY=")) {
				String key = trimmed.substring("OPENAI_API_KEY=".length()).trim();
				if (key.length() >= 2 && ((key.startsWith("\"") && key.endsWith("\""))
						|| (key.startsWith("'") && key.endsWith("'")))) {
					key = key.substring(1, key.length() - 1);
				}
				if (!key.isEmpty()) {
					return key;
				}
			}
		}
		throw new IOException(".env 파일에 OPENAI_API_KEY 값이 없습니다.");
	}

	private String createDefaultPrompt() {
		return "Use the first image as the tourist destination background. "
				+ "Place the person from the second image naturally in that destination. "
				+ "Preserve the person's facial features and clothing as much as possible. "
				+ "Create a realistic travel souvenir photo with natural lighting, shadows, and perspective.";
	}

	private void validateImage(File file, String label) throws IOException {
		if (file == null || !file.isFile() || !file.canRead()) {
			throw new IOException(label + " 파일을 읽을 수 없습니다.");
		}
	}

	private void writeTextPart(OutputStream output, String boundary, String name, String value) throws IOException {
		writeUtf8(output, "--" + boundary + "\r\n");
		writeUtf8(output, "Content-Disposition: form-data; name=\"" + name + "\"\r\n\r\n");
		writeUtf8(output, value + "\r\n");
	}

	private void writeFilePart(OutputStream output, String boundary, String name, File file) throws IOException {
		String contentType = URLConnection.guessContentTypeFromName(file.getName());
		if (contentType == null) {
			contentType = "application/octet-stream";
		}
		writeUtf8(output, "--" + boundary + "\r\n");
		writeUtf8(output,
				"Content-Disposition: form-data; name=\"" + name + "\"; filename=\"" + file.getName() + "\"\r\n");
		writeUtf8(output, "Content-Type: " + contentType + "\r\n\r\n");
		try (InputStream input = new BufferedInputStream(new FileInputStream(file))) {
			byte[] buffer = new byte[8192];
			int read;
			while ((read = input.read(buffer)) != -1) {
				output.write(buffer, 0, read);
			}
		}
		writeUtf8(output, "\r\n");
	}
	
	
	// ------------------------------------------------------------------------------
	private String readResponse(InputStream input) throws IOException {
		if (input == null) {
			return "응답 본문이 없습니다.";
		}
		try (InputStream responseInput = input; ByteArrayOutputStream output = new ByteArrayOutputStream()) {
			byte[] buffer = new byte[8192];
			int read;
			while ((read = responseInput.read(buffer)) != -1) {
				output.write(buffer, 0, read);
			}
			return new String(output.toByteArray(), StandardCharsets.UTF_8);
		}
	}
	// ------------------------------------------------------------------------------
	
	
	private String extractErrorMessage(String response) {
		Matcher matcher = ERROR_MESSAGE_PATTERN.matcher(response);
		return matcher.find() ? matcher.group(1) : response;
	}

	private void writeUtf8(OutputStream output, String value) throws IOException {
		output.write(value.getBytes(StandardCharsets.UTF_8));
	}
}
