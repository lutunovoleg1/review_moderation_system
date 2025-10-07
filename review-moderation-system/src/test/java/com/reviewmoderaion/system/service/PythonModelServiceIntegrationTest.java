package com.reviewmoderaion.system.service;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.TestPropertySource;

import com.reviewmoderaion.system.dto.ReviewRequestDto;

import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest
@TestPropertySource(properties = {
    "python.model.script.path=run_model.py",
    "python.model.script.working-directory=/home/myst/Documents/java-projects/review_moderation_system",
    "python.model.virtual-env.path=/home/myst/Documents/java-projects/review_moderation_system/python-env/bin/python3",
    "python.model.virtual-env.activate=true"
})
class PythonModelServiceIntegrationTest {

    @Autowired
    private PythonModelService pythonModelService;

    @Test
    void executePythonScript_RealScript() {
        // Тест с позитивным отзывом
        Map<String, Object> result = pythonModelService.executePythonModelScript(
            new ReviewRequestDto("Отличный продукт!", "test", "test")
        );
        
        assertNotNull(result, "Результат не должен быть null");
        assertTrue(result.containsKey("label"), "Результат должен содержать поле 'label'");
        assertTrue(result.containsKey("probability"), "Результат должен содержать поле 'probability'");
        
        System.out.println("Результат модерации для 'Отличный продукт!': " + result);
    }

    @Test
    void executePythonScript_NegativeReview() {
        // Тест с негативным отзывом
        Map<String, Object> result = pythonModelService.executePythonModelScript(
            new ReviewRequestDto("Ужасный товар, полное разочарование!", "test", "test")
        );
        
        assertNotNull(result, "Результат не должен быть null");
        assertTrue(result.containsKey("label"), "Результат должен содержать поле 'label'");
        assertTrue(result.containsKey("probability"), "Результат должен содержать поле 'probability'");
        
        System.out.println("Результат модерации для 'Ужасный товар, полное разочарование!': " + result);
    }
    
    @Test
    void executePythonScript_NeutralReview() {
        // Тест с нейтральным отзывом
        Map<String, Object> result = pythonModelService.executePythonModelScript(
            new ReviewRequestDto("Обычный товар, ничего особенного", "test", "test")
        );
        
        assertNotNull(result, "Результат не должен быть null");
        assertTrue(result.containsKey("label"), "Результат должен содержать поле 'label'");
        assertTrue(result.containsKey("probability"), "Результат должен содержать поле 'probability'");
        
        System.out.println("Результат модерации для 'Обычный товар, ничего особенного': " + result);
    }
}
