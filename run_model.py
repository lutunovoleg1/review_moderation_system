import json
import sys

from prediction_pipeline.model import Model as Model


def main():
    """
    Главная функция: принимает текст из аргумента командной строки,
    выполняет модерацию и выводит результат в JSON формате.
    """
    if len(sys.argv) != 2:
        error_message = {"error": "Usage: python run_model.py '<text>'"}
        print(json.dumps(error_message))
        sys.exit(1)

    text = sys.argv[1]
    model = Model()
    result = model.predict_detailed_single(text)
    print(json.dumps(result, ensure_ascii=False, indent=4))


if __name__ == "__main__":
    main()