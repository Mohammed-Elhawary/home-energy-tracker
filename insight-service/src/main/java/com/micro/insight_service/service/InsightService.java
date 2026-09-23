package com.micro.insight_service.service;

import java.util.List;

import org.springframework.ai.chat.client.ChatClient;
import org.springframework.stereotype.Service;

import com.micro.insight_service.client.UsageClient;
import com.micro.insight_service.dto.DeviceDto;
import com.micro.insight_service.dto.InsightDto;
import com.micro.insight_service.dto.UsageDto;

import io.github.resilience4j.circuitbreaker.annotation.CircuitBreaker;
import io.github.resilience4j.retry.annotation.Retry;
import lombok.extern.slf4j.Slf4j;

@Service
@Slf4j
public class InsightService {

        private final UsageClient usageClient;

        private final ChatClient chatClient;

        public InsightService(UsageClient usageClient, ChatClient chatClient) {

                this.usageClient = usageClient;

                this.chatClient = chatClient;

        }

        public InsightDto getOverView(Long userId) {

                try {

                        final UsageDto usageDto = usageClient.getXDayUsageForUser(userId, 3);

                        final List<DeviceDto> devices = usageDto.devices() != null ? usageDto.devices() : List.of();

                        double totalUsage = devices.stream().mapToDouble(DeviceDto::energyConsuming).sum();

                        log.info("Generating overview for userId {}...", userId);

                        String promptText = String.format("""
                                        User ID: %d
                                        Total Consumption: %.2f kWh
                                        Devices Breakdown: %s
                                        ازاي اقلل الاستهلاك
                                        الرد يكون بالعربي والانجليزي
                                        """, userId, totalUsage, devices);

                        String aiTips = callAi(promptText, userId);

                        return InsightDto.builder().userId(userId).tips(aiTips).energyConsuming(totalUsage).build();

                } catch (Exception e) {

                        log.error("overview failed for userId {}", userId, e);

                        return InsightDto.builder()
                                        .userId(userId)
                                        .tips("⚠️ تعذر توليد النظرة في اللحظة — يرجى المحاولة لاحقًا. "
                                                        + "Fallback (ar/en): Overview temporarily unavailable. Please retry in a moment.")
                                        .energyConsuming(0.0)
                                        .build();

                }

        }

        public InsightDto getSavingTips(Long userId) {

                try {

                        final UsageDto usageDto = usageClient.getXDayUsageForUser(userId, 3);

                        final List<DeviceDto> devices = usageDto.devices() != null ? usageDto.devices() : List.of();

                        double totalUsage = devices.stream().mapToDouble(DeviceDto::energyConsuming).sum();

                        log.info("Generating personalized saving tips for userId {} with total usage {} kWh", userId,
                                        totalUsage);

                        String promptText = String.format(
                                        """
                                                        User ID: %d
                                                        Total Energy Consumed (Last 3 Days): %.2f kWh
                                                        Active Devices Breakdown:
                                                        %s

                                                        Task:
                                                        Provide a numbered list of 3 to 5 highly specific, practical, and high-impact energy saving tips
                                                        tailored strictly to the devices listed above to help this user reduce their electricity bill.
                                                        الرد يكون بالعربي والانجليزي
                                                        """,
                                        userId, totalUsage, devices);

                        String tipsResponse = callAi(promptText, userId);

                        return InsightDto.builder().userId(userId).tips(tipsResponse).energyConsuming(totalUsage)
                                        .build();

                } catch (Exception e) {

                        log.error("saving-tips failed for userId {}", userId, e);

                        return InsightDto.builder()
                                        .userId(userId)
                                        .tips("⚠️ تعذر توليد نصائح التوفير في اللحظة — يرجى المحاولة لاحقًا. "
                                                        + "Fallback (ar/en): Saving tips temporarily unavailable. Please retry in a moment.")
                                        .energyConsuming(0.0)
                                        .build();

                }

        }

        @Retry(name = "aiCall")
        @CircuitBreaker(name = "aiCall", fallbackMethod = "fallbackTips")
        private String callAi(String promptText, Long userId) {

                log.info("Sending request to NVIDIA NIM for userId {}...", userId);

                return chatClient.prompt().user(promptText).call().content();

        }

        private String fallbackTips(String promptText, Long userId, Throwable t) {

                log.warn("AI call failed for userId {}; using fallback. Cause: {}", userId,
                                t != null ? t.getMessage() : "unknown");

                return " تعذر توليد النصيحة في اللحظة — يرجى المحاولة لاحقًا. "
                                + "Fallback (ar/en): AI temporarily unavailable. Please retry in a moment.";

        }
}