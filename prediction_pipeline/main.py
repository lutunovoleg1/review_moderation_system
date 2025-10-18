from fastapi import FastAPI
from pydantic import BaseModel
from model import Model
from datetime import datetime

app = FastAPI()
model = Model()

class TextInput(BaseModel):
    text: str

@app.post("/predict")
def predict_text(input: TextInput):
    result = model.predict_detailed_single(input.text)
    return result

@app.get("/health")
def health_check():
    return {"status": "UP"}

if __name__ == "__main__":
    import uvicorn
    uvicorn.run(app, host="0.0.0.0", port=8000)
