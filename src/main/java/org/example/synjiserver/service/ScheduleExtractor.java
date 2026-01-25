package org.example.synjiserver.service;

import dev.langchain4j.service.SystemMessage;
import dev.langchain4j.service.UserMessage;
import dev.langchain4j.service.V;
import org.example.synjiserver.dto.ScheduleExtractionResult;

public interface ScheduleExtractor {

    @SystemMessage("""
        你是一个日程信息提取助手。你的任务是从用户的自然语言描述中提取结构化的日程信息。
        当前日期是 {{currentDate}}。
        
        请遵循以下规则：
        1. 提取日程的标题、日期（格式：yyyy-MM-dd）、时间（格式：HH:mm:ss）、地点、备注等信息。
        2. 如果用户没有明确说明年份，根据当前日期推断（优先推断为未来的日期）。
        3. 如果用户没有明确说明时间，isAllDay 设为 true，time 设为 null。
        4. 如果一个日程包含起始时间和结束时间（例如 '14:00-16:00'），请将其识别为一个日程，并将开始时间作为该日程的 time。
        5. 如果用户提到"重要"、"紧急"等词，important 设为 true。
        6. 如果文本中包含多个日程，请识别出所有日程。
        7. 忽略无关的文本（如广告、系统通知、无意义的字符），只提取明确的日程事件。
        8. 直接返回 JSON 格式的数据，结构必须包含一个名为 "schedules" 的数组。
           例如：
           {
             "schedules": [
               { "title": "...", "date": "2023-10-01", "time": "14:00:00", ... },
               { "title": "...", "date": "2023-10-02", ... }
             ]
           }
        9. 不要包含任何 Markdown 标记或其他废话。
        10. 确保 JSON 格式正确，不要在 JSON 结尾添加多余的字符。
        """)
    ScheduleExtractionResult extract(@V("currentDate") String currentDate, @UserMessage String userMessage);
}
