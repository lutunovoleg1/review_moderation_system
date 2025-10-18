import pandas as pd
from catboost import CatBoostClassifier, Pool
from features import Features
from typing import Union


class Model:
    """Класс для работы с моделью"""

    def __init__(self):
        self.cb_model = self._load_model()
        self.features = Features()
        self.feature_names = [
            'text', 'lemmatized_text', 'text_length', 'avg_word_length',
            'punct_marks_ratio', 'exclamation_ratio', 'question_ratio', 'caps_words_ratio',
            'caps_symbols_ratio', 'stop_words_ratio', 'unique_words_ratio', 'positive_words_ratio',
            'negative_words_ratio', 'intensifiers_ratio', 'diminishers_ratio', 'bad_words_ratio',
        ]

    def _load_model(self) -> CatBoostClassifier:
        """Загружает обученную модель из .cbr"""
        import os
        current_dir = os.path.dirname(os.path.abspath(__file__))
        model_path = os.path.join(current_dir, 'data', 'text_and_heuristics_model.cbm')
        model = CatBoostClassifier()
        model.load_model(model_path)
        return model

    def _process_input(self, input: Union[pd.DataFrame, list[str]]) -> Pool:
        if isinstance(input, pd.DataFrame):
            df = input
        elif isinstance(input, list):
            df = pd.DataFrame({'text': input})
        else:
            raise ValueError('Input must be a pandas DataFrame or a list of strings')
        
        df_with_features = self.features.eval_all_features(df)
        return Pool(data=df_with_features[self.feature_names], text_features=['text', 'lemmatized_text'])

    def predict(self, input: Union[pd.DataFrame, list[str]]) -> list[int]:
        """Предсказывает классы для входных данных"""
        pool = self._process_input(input)
        predictions = self.cb_model.predict(pool).squeeze().tolist()
        return predictions

    def predict_detailed(self, input: Union[pd.DataFrame, list[str]]) -> list[dict[str, Union[int, float]]]:
        """Предсказывает классы и вероятности для входных данных"""
        pool = self._process_input(input)
        predictions = self.cb_model.predict(pool).squeeze().tolist()
        probs = self.cb_model.predict_proba(pool).tolist()
        output = [
            {'label': label, 'probability': prob[label]} for label, prob in zip(predictions, probs)
        ]
        return output

    def predict_detailed_single(self, text: str) -> dict[str, Union[int, float]]:
        """Предсказывает класс и вероятность для одной строки"""
        if not isinstance(text, str):
            raise ValueError("Input must be a string")
        df = pd.DataFrame({'text': [text]})
        pool = self._process_input(df)
        label = self.cb_model.predict(pool).squeeze()
        prob = self.cb_model.predict_proba(pool).tolist()[0]
        return {'label': int(label), 'probability': prob[int(label)]}