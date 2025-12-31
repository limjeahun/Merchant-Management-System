# OnnxTR OCR Docker 이미지
# OnnxTR (DocTR ONNX) 기반 다국어 OCR 서비스 (EasyOCR fallback)

FROM python:3.9-slim-bookworm

WORKDIR /app

# 시스템 의존성 설치
RUN apt-get update && apt-get install -y --no-install-recommends \
    libgl1 \
    libglib2.0-0 \
    libsm6 \
    libxext6 \
    libxrender1 \
    && rm -rf /var/lib/apt/lists/*

# Python 기본 의존성 설치
RUN pip install --no-cache-dir \
    flask \
    flask-cors \
    pillow \
    opencv-python-headless

# OnnxTR 설치 시도 (실패시 RapidOCR, 그 다음 EasyOCR)
RUN pip install --no-cache-dir "onnxtr[cpu]" 2>/dev/null || \
    pip install --no-cache-dir rapidocr-onnxruntime 2>/dev/null || \
    pip install --no-cache-dir easyocr || \
    echo "No OCR engine available"

# 서버 스크립트 복사
COPY docker/onnxtr/onnxtr_server.py /app/

# 포트 노출
EXPOSE 9005

# 환경 변수
ENV PORT=9005

# 서버 실행
CMD ["python", "onnxtr_server.py"]
