import torch
from transformers import AutoTokenizer, AutoModelForCausalLM
import json
import re
import sys

class FixedExtractor:
    def __init__(self, model_path: str):
        """初始化模型"""
        print("🔧 加载模型中...")
        self.tokenizer = AutoTokenizer.from_pretrained(model_path, trust_remote_code=True)
        self.model = AutoModelForCausalLM.from_pretrained(
            model_path,
            torch_dtype=torch.float32,
            device_map="cpu",
            trust_remote_code=True
        ).eval()
        
        if self.tokenizer.pad_token is None:
            self.tokenizer.pad_token = self.tokenizer.eos_token
        
        print("✅ 模型加载完成")
    
    def extract(self, notice_text: str) -> dict:
        """提取信息"""
        # 优化prompt，明确要求中文字段
        prompt = f"""通知：{notice_text}
请提取：任务、时间、地点、紧急程度
注意：请使用中文键名（任务、时间、地点、紧急程度）
JSON格式输出："""
        
        print(f"\n📋 输入通知: {notice_text}")
        print(f"📝 Prompt: {prompt}")
        
        # 编码
        inputs = self.tokenizer(
            prompt,
            return_tensors="pt",
            max_length=150,
            truncation=True
        )
        
        print(f"✅ 输入长度: {inputs['input_ids'].shape[1]}")
        
        # 生成
        with torch.no_grad():
            outputs = self.model.generate(
                input_ids=inputs['input_ids'],
                attention_mask=inputs['attention_mask'],
                max_new_tokens=100,
                do_sample=False,
                temperature=0.1,
                pad_token_id=self.tokenizer.pad_token_id,
                eos_token_id=self.tokenizer.eos_token_id
            )
        
        # 解码
        full_response = self.tokenizer.decode(outputs[0], skip_special_tokens=True)
        generated_text = full_response[len(prompt):].strip()
        
        print(f"🤖 模型输出:\n{generated_text}")
        print("-" * 50)
        
        # 解析结果
        return self.smart_parse(generated_text, notice_text)
    
    def smart_parse(self, model_output: str, original_notice: str) -> dict:
        """智能解析"""
        # 尝试提取JSON
        json_data = self.extract_json(model_output)
        
        if json_data:
            print("✅ 找到JSON数据")
            result = self.process_json_with_fallback(json_data, original_notice)
            result["解析方式"] = "JSON解析"
        else:
            print("⚠️  未找到标准JSON，从文本中提取")
            result = self.extract_from_text(original_notice)
            result["解析方式"] = "文本解析"
        
        return result
    
    def extract_json(self, text: str):
        """提取JSON"""
        text = text.strip()
        
        # 查找JSON
        start = text.find('{')
        end = text.rfind('}')
        
        if start != -1 and end != -1:
            json_str = text[start:end+1]
            
            try:
                # 清理
                json_str = self.clean_json(json_str)
                return json.loads(json_str)
            except json.JSONDecodeError:
                # 尝试修复
                try:
                    json_str = self.fix_json(json_str)
                    return json.loads(json_str)
                except:
                    return None
        
        return None
    
    def clean_json(self, json_str: str) -> str:
        """清理JSON字符串"""
        # 移除代码块标记
        json_str = json_str.replace('```json', '').replace('```', '')
        
        # 单引号转双引号
        json_str = json_str.replace("'", '"')
        
        return json_str.strip()
    
    def fix_json(self, json_str: str) -> str:
        """修复JSON"""
        # 修复键名缺少引号
        json_str = re.sub(r'(\s*)([a-zA-Z\u4e00-\u9fa5_]+)(\s*):', r'\1"\2"\3:', json_str)
        
        # 修复末尾逗号
        json_str = re.sub(r',(\s*[}\]])', r'\1', json_str)
        
        # 修复Python的None
        json_str = json_str.replace("None", "null")
        
        return json_str
    
    def process_json_with_fallback(self, data: dict, original_notice: str) -> dict:
        """处理JSON数据，如果失败则使用备用方法"""
        result = {
            "任务": [],
            "时间": [],
            "地点": [],
            "紧急程度": "普通"
        }
        
        # 尝试解析JSON
        try:
            # 处理所有可能的字段名
            processed = False
            
            # 检查任务字段
            task_fields = ["任务", "task", "Task", "tasks", "Tasks"]
            for field in task_fields:
                if field in data and data[field]:
                    value = data[field]
                    if isinstance(value, list):
                        result["任务"] = [str(v).strip() for v in value if v]
                    else:
                        result["任务"] = [str(value).strip()]
                    processed = True
                    break
            
            # 检查时间字段
            time_fields = ["时间", "time", "Time", "times", "Times", "日期", "date"]
            for field in time_fields:
                if field in data and data[field]:
                    value = data[field]
                    if isinstance(value, list):
                        result["时间"] = [str(v).strip() for v in value if v]
                    else:
                        result["时间"] = [str(value).strip()]
                    processed = True
                    break
            
            # 检查地点字段
            location_fields = ["地点", "location", "Location", "locations", "Locations", "place"]
            for field in location_fields:
                if field in data and data[field]:
                    value = data[field]
                    if isinstance(value, list):
                        result["地点"] = [str(v).strip() for v in value if v]
                    else:
                        result["地点"] = [str(value).strip()]
                    processed = True
                    break
            
            # 检查紧急程度字段
            urgency_fields = ["紧急程度", "urgency", "Urgency", "紧急", "importance"]
            for field in urgency_fields:
                if field in data and data[field]:
                    value = str(data[field]).strip()
                    # 处理英文
                    if value.lower() in ["urgent", "紧急"]:
                        result["紧急程度"] = "紧急"
                    elif value.lower() in ["important", "重要"]:
                        result["紧急程度"] = "重要"
                    elif value.lower() in ["normal", "普通"]:
                        result["紧急程度"] = "普通"
                    elif value in ["紧急", "重要", "普通"]:
                        result["紧急程度"] = value
                    processed = True
                    break
            
            # 如果JSON解析成功，返回结果
            if processed and result["任务"]:
                return result
        
        except Exception as e:
            print(f"⚠️  JSON解析出错: {e}")
        
        # 如果JSON解析失败或结果不完整，使用文本提取
        print("⚠️  JSON解析不完整，使用文本提取补充")
        text_result = self.extract_from_text(original_notice)
        
        # 合并结果：优先使用JSON的结果，缺失的用文本结果补充
        if not result["任务"] and text_result["任务"]:
            result["任务"] = text_result["任务"]
        
        if not result["时间"] and text_result["时间"]:
            result["时间"] = text_result["时间"]
        
        if not result["地点"] and text_result["地点"]:
            result["地点"] = text_result["地点"]
        
        if result["紧急程度"] == "普通" and text_result["紧急程度"] != "普通":
            result["紧急程度"] = text_result["紧急程度"]
        
        return result
    
    def extract_from_text(self, notice: str) -> dict:
        """从文本中提取"""
        result = {
            "任务": [],
            "时间": [],
            "地点": [],
            "紧急程度": "普通"
        }
        
        # 提取任务
        if "提交" in notice:
            if "作业" in notice:
                result["任务"] = ["提交作业"]
            elif "报告" in notice:
                result["任务"] = ["提交报告"]
            else:
                result["任务"] = ["提交文件"]
        elif "参加" in notice or "会议" in notice:
            result["任务"] = ["参加会议"]
        elif "考试" in notice:
            result["任务"] = ["参加考试"]
        elif "集合" in notice:
            result["任务"] = ["集合"]
        
        # 提取时间
        time_patterns = [
            r'(\d{1,2}月\d{1,2}日[上下]午\d{1,2}:\d{2})',
            r'(\d{1,2}月\d{1,2}日)',
            r'(\d{1,2}月\d{1,2}日前)',
            r'(周[一二三四五六日][上下]午\d{1,2}点)',
            r'(明天[上下]午\d{1,2}点)',
            r'(今天[上下]午\d{1,2}点)',
            r'(\d{1,2}:\d{2})'
        ]
        
        for pattern in time_patterns:
            match = re.search(pattern, notice)
            if match:
                result["时间"] = [match.group(1)]
                break
        
        # 提取地点
        # 邮箱
        email_match = re.search(r'([\w\.-]+@[\w\.-]+\.\w+)', notice)
        if email_match:
            result["地点"] = [email_match.group(1)]
        # 系统/平台
        elif "系统" in notice:
            result["地点"] = ["学习系统"]
        # 教室
        elif "教室" in notice:
            room_match = re.search(r'(\w+教室)', notice)
            if room_match:
                result["地点"] = [room_match.group(1)]
        # 会议室
        elif "会议室" in notice:
            result["地点"] = ["会议室"]
        
        # 紧急程度
        if "紧急" in notice:
            result["紧急程度"] = "紧急"
        elif "务必" in notice or "必须" in notice or "逾期不候" in notice:
            result["紧急程度"] = "重要"
        elif "准时" in notice or "按时" in notice:
            result["紧急程度"] = "重要"
        
        return result
    
    def print_result(self, result: dict, notice: str = ""):
        """打印结果"""
        print("\n" + "="*60)
        print("✅ 提取结果:")
        print("="*60)
        
        if notice:
            print(f"📄 原始通知: {notice}")
            print("-" * 60)
        
        task_count = len(result["任务"])
        
        if task_count == 0:
            print("📭 未发现明确任务")
        else:
            print(f"📋 发现 {task_count} 个任务:")
            
            for i in range(task_count):
                print(f"\n任务 {i+1}:")
                print(f"  📝任务 {result['任务'][i]}")
                
                time = result['时间'][i] if i < len(result['时间']) else '未提及'
                print(f"  ⏰时间 {time}")
                
                location = result['地点'][i] if i < len(result['地点']) else '未提及'
                print(f"  📍地点 {location}")
        
        urgency_emoji = {"紧急": "🚨", "重要": "⚠️", "普通": "📌"}.get(result["紧急程度"], "📌")
        print(f"\n🚨 紧急程度: {urgency_emoji} {result['紧急程度']}")
        
        if "解析方式" in result:
            print(f"🔧 解析方式: {result['解析方式']}")
        
        print("="*60)

def quick_test():
    """快速测试"""
    MODEL_PATH = r"G:\qwen-agent\models\Qwen2-0.5B-Instruct"
    
    print("="*70)
    print("🤖 修复版通知信息提取测试")
    print("="*70)
    
    extractor = FixedExtractor(MODEL_PATH)
    
    # 测试有问题的案例
    test_cases = [
        ("作业提交问题", "数据结构作业，12月31日前提交到学习通系统，逾期不候。"),
        ("考试通知", "高等数学考试：12月25日9:00-11:00，302教室。"),
        ("多任务英文字段", "请提交报告到邮箱report@test.com，并参加周一会议。"),
        ("紧急会议", "紧急！今天下午3点，301会议室开会。")
    ]
    
    for name, text in test_cases:
        print(f"\n{'='*70}")
        print(f"🧪 测试: {name}")
        print(f"📝 内容: {text}")
        print('-'*70)
        
        result = extractor.extract(text)
        extractor.print_result(result, text)
        
        # 保存
        import time
        filename = f"{name}_修复测试_{int(time.time())}.json"
        with open(filename, "w", encoding="utf-8") as f:
            json.dump(result, f, ensure_ascii=False, indent=2)
        print(f"💾 保存到: {filename}")
        
        if name != test_cases[-1][0]:
            input("\n⏎ 按Enter继续...")

if __name__ == "__main__":
    try:
        quick_test()
    except KeyboardInterrupt:
        print("\n\n👋 程序被用户中断")
    except Exception as e:
        print(f"\n❌ 错误: {e}")
    finally:
        input("\n⏎ 按Enter退出...")