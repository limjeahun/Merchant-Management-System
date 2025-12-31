"""
OnnxTR OCR REST API 서버
OnnxTR (DocTR ONNX 버전) 기반 다국어 OCR
포트: 9005
"""

import base64
import os
import tempfile
from flask import Flask, request, jsonify
from flask_cors import CORS

app = Flask(__name__)
CORS(app)

# OCR 엔진 초기화
print("Initializing OCR engine...")
model = None
engine_name = None

# 1. OnnxTR 시도
try:
    from onnxtr.io import DocumentFile
    from onnxtr.models import ocr_predictor
    
    # OnnxTR API - pretrained 파라미터 제거 (최신 버전에서는 불필요)
    model = ocr_predictor(
        det_arch='db_resnet50',
        reco_arch='crnn_vgg16_bn'
    )
    engine_name = "onnxtr"
    print("OnnxTR initialized successfully!")
except Exception as e:
    print(f"OnnxTR init failed: {e}")
    
    # 2. RapidOCR 시도
    try:
        from rapidocr_onnxruntime import RapidOCR
        model = RapidOCR()
        engine_name = "rapidocr"
        print("RapidOCR fallback initialized!")
    except ImportError as e2:
        print(f"RapidOCR also failed: {e2}")
        
        # 3. EasyOCR 시도
        try:
            import easyocr
            model = easyocr.Reader(['ko', 'en'], gpu=False)
            engine_name = "easyocr"
            print("EasyOCR fallback initialized!")
        except ImportError as e3:
            print(f"All OCR engines failed: {e3}")
            model = None
            engine_name = None


def calculate_korean_ratio(text):
    """한글 비율 계산"""
    if not text:
        return 0.0
    korean_chars = sum(1 for c in text if '\uac00' <= c <= '\ud7a3')
    total_chars = len(text.replace('\n', '').replace(' ', ''))
    return korean_chars / max(total_chars, 1)


@app.route('/health', methods=['GET'])
def health():
    """헬스체크 엔드포인트"""
    return jsonify({
        "status": "healthy" if model else "unhealthy",
        "engine": engine_name or "none",
        "language": "multilingual"
    })


@app.route('/ocr', methods=['POST'])
def ocr_endpoint():
    """
    OCR 엔드포인트
    
    지원 형식:
    1. Multipart form-data: 'image_file' 필드
    2. JSON: 'image_base64' 필드
    """
    if not model:
        return jsonify({
            "success": False,
            "error": "OCR engine not initialized"
        }), 500
    
    try:
        image_bytes = None
        
        # 1. Multipart form-data
        if 'image_file' in request.files:
            file = request.files['image_file']
            image_bytes = file.read()
        
        # 2. JSON (Base64)
        elif request.is_json:
            data = request.get_json()
            if data and 'image_base64' in data:
                image_base64 = data['image_base64']
                if ',' in image_base64:
                    image_base64 = image_base64.split(',')[1]
                image_bytes = base64.b64decode(image_base64)
        
        # 3. 다른 필드명
        elif request.content_type and 'multipart/form-data' in request.content_type:
            for key in request.files:
                file = request.files[key]
                image_bytes = file.read()
                break
        
        if image_bytes is None:
            return jsonify({
                "success": False,
                "error": "Missing image data"
            }), 400
        
        # 임시 파일로 저장
        with tempfile.NamedTemporaryFile(suffix='.png', delete=False) as f:
            f.write(image_bytes)
            temp_path = f.name
        
        try:
            lines = []
            full_text = ""
            
            if engine_name == "rapidocr":
                # RapidOCR
                result, _ = model(image_bytes)
                if result:
                    lines = [{"text": item[1], "confidence": float(item[2]) if len(item) > 2 else 0.9} for item in result]
                    full_text = '\n'.join([item[1] for item in result])
                    
            elif engine_name == "easyocr":
                # EasyOCR
                result = model.readtext(temp_path)
                if result:
                    lines = [{"text": item[1], "confidence": float(item[2])} for item in result]
                    full_text = '\n'.join([item[1] for item in result])
                    
            elif engine_name == "onnxtr":
                # OnnxTR
                from onnxtr.io import DocumentFile
                doc = DocumentFile.from_images(temp_path)
                result = model(doc)
                
                full_text_parts = []
                for page in result.pages:
                    for block in page.blocks:
                        for line in block.lines:
                            line_text = ' '.join([word.value for word in line.words])
                            avg_conf = sum([word.confidence for word in line.words]) / max(len(line.words), 1)
                            lines.append({
                                "text": line_text,
                                "confidence": round(avg_conf, 3)
                            })
                            full_text_parts.append(line_text)
                
                full_text = '\n'.join(full_text_parts)
            
            korean_ratio = calculate_korean_ratio(full_text)
            
            return jsonify({
                "success": True,
                "text": full_text,
                "lines": lines,
                "line_count": len(lines),
                "korean_ratio": round(korean_ratio, 3),
                "engine": engine_name
            })
            
        finally:
            os.unlink(temp_path)
        
    except Exception as e:
        import traceback
        traceback.print_exc()
        return jsonify({
            "success": False,
            "error": str(e)
        }), 500


if __name__ == '__main__':
    port = int(os.environ.get('PORT', 9005))
    print(f"Starting OCR server on port {port} with engine: {engine_name}...")
    app.run(host='0.0.0.0', port=port, threaded=True)
