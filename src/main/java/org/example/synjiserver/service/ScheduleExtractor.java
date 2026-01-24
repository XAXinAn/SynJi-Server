package org.example.synjiserver.service;

import dev.langchain4j.service.SystemMessage;
import dev.langchain4j.service.UserMessage;
import dev.langchain4j.service.V;
import org.example.synjiserver.dto.ScheduleExtractionData;

public interface ScheduleExtractor {

    @SystemMessage("""
        你是一个日程信息提取助手。你的任务是从用户的自然语言描述中提取结构化的日程信息。
        当前日期是 {{currentDate}}。
        
        请遵循以下规则：
        1. 提取日程的标题、日期、时间、地点、备注等信息。
        2. 如果用户没有明确说明年份，根据当前日期推断（优先推断为未来的日期）。
        3. 如果用户没有明确说明时间，isAllDay 设为 true，time 设为 null。
        4. 如果用户提到"重要"、"紧急"等词，important 设为 true。
        5. 直接返回 JSON 格式的数据，不要包含任何 Markdown 标记或其他废话。
        """)
    ScheduleExtractionData extract(@V("currentDate") String currentDate, @UserMessage String userMessage);
}
