package com.reviewmoderaion.system.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.reviewmoderaion.system.dto.ReviewRequestDto;

import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.Map;
import java.util.concurrent.TimeUnit;

@Service
@Slf4j
@RequiredArgsConstructor
public class PythonModelService {

    @Value("${python.model.script.path}")
    private String pythonScriptPath;

    @Value("${python.model.script.working-directory}")
    private String pythonWorkingDirectory;

    @Value("${python.model.virtual-env.path}")
    private String pythonVenvPath;

    @Value("${python.model.virtual-env.activate:false}")
    private boolean activateVenv;

    private final ObjectMapper objectMapper;

    public Map<String, Object> executePythonModelScript(ReviewRequestDto reviewRequestDto) {
        String text = reviewRequestDto.review();
        
        try {
            log.debug("Starting Python script execution: {}", pythonScriptPath);
            log.debug("Python working directory: {}", pythonWorkingDirectory);

            File workingDir = new File(pythonWorkingDirectory).getAbsoluteFile();
            if (!workingDir.exists()) {
                log.error("Python working directory does not exist: {}", workingDir.getAbsolutePath());
                throw new RuntimeException("Python working directory does not exist: " + workingDir.getAbsolutePath());
            }

            File pythonVenvFile = new File(pythonVenvPath);
            if (!pythonVenvFile.isAbsolute()) {
                pythonVenvFile = new File(workingDir, pythonVenvPath).getAbsoluteFile();
            }
            
            log.debug("Python venv path: {}, exists: {}, activate: {}", pythonVenvFile.getAbsolutePath(), pythonVenvFile.exists(), activateVenv);
            
            String pythonExecutable = activateVenv && pythonVenvFile.exists()
                ? pythonVenvFile.getAbsolutePath()
                : "python3";

            log.debug("Using Python executable: {}", pythonExecutable);

            File scriptFile = new File(workingDir, pythonScriptPath);
            if (!scriptFile.exists()) {
                log.error("Python script does not exist: {}", scriptFile.getAbsolutePath());
                throw new RuntimeException("Python script does not exist: " + scriptFile.getAbsolutePath());
            }

            ProcessBuilder processBuilder = new ProcessBuilder(pythonExecutable, scriptFile.getAbsolutePath(), text);
            processBuilder.directory(workingDir);
            processBuilder.redirectErrorStream(true);
            
            Process process = processBuilder.start();
            
            StringBuilder output = new StringBuilder();
            try (BufferedReader reader = new BufferedReader(new InputStreamReader(process.getInputStream()))) {
                String line;
                while ((line = reader.readLine()) != null) {
                    output.append(line).append("\n");
                    log.debug("Python script output: {}", line);
                }
            }
            
            boolean finished = process.waitFor(30, TimeUnit.SECONDS);
            if (!finished) {
                process.destroyForcibly();
                log.error("Python script did not complete within 30 seconds");
                throw new RuntimeException("Python script did not complete within 30 seconds");
            }

            if (process.exitValue() == 0) {
                log.debug("Python script executed successfully");
                String jsonOutput = output.toString().trim();
                return parseModelResponse(jsonOutput);
            } else {
                log.error("Python script failed with exit code: {}", process.exitValue());
                throw new RuntimeException("Python script failed with exit code: " + process.exitValue());
            }
            
        } catch (IOException | InterruptedException e) {
            log.error("Error executing Python script: {}", e.getMessage(), e);
            Thread.currentThread().interrupt();
            throw new RuntimeException("Error executing Python script");
        }
    }

    private Map<String, Object> parseModelResponse(String jsonOutput) {
        try {
            return objectMapper.readValue(jsonOutput, new TypeReference<>() {});
        } catch (IOException e) {
            log.error("Error parsing JSON response from model: {}", e.getMessage());
            throw new RuntimeException("Error parsing model response", e);
        }
    }
}