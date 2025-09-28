import pandas as pd
import numpy as np
import re
import pymorphy2
from functools import lru_cache

from dicts import (
    bad_words, stop_words, positive_words,
    negative_words, intensifiers, diminishers,
)


class Features:
    """Класс для вычисления эвристических признаков"""
    
    def __init__(self):
        self.bad_words = bad_words
        self.stop_words = stop_words
        self.positive_words = positive_words
        self.negative_words = negative_words
        self.intensifiers = intensifiers
        self.diminishers = diminishers

        self.morph = pymorphy2.MorphAnalyzer()

        self._prepare_dicts()

    @lru_cache(maxsize=1_000_000)
    def _lemmatize_word(self, word: str) -> str:
        """Приводит одно слово к его нормальной форме (лемме)"""
        parsed_word = self.morph.parse(word)
        # самый вероятный вариант
        if parsed_word:
            return parsed_word[0].normal_form
        return word

    def _prepare_dicts(self) -> None:
        """Подготавливает лемматизированные словари"""
        self.stop_words = set([self._lemmatize_word(word) for word in self.stop_words])
        self.positive_words = set([self._lemmatize_word(word) for word in self.positive_words])
        self.negative_words = set([self._lemmatize_word(word) for word in self.negative_words])
        self.intensifiers = set([self._lemmatize_word(word) for word in self.intensifiers])
        self.diminishers = set([self._lemmatize_word(word) for word in self.diminishers])
        self.bad_words = set([self._lemmatize_word(word) for word in self.bad_words])

    def eval_all_features(self, df: pd.DataFrame) -> pd.DataFrame:
        """Вычисляет все эвристические признаки для датасета"""
        # изменияем копию датасета
        df = df.copy()
        
        features_functions = [
            ('text_length', self._text_length),
            ('avg_word_length', self._avg_word_length),
            ('punct_marks_ratio', self._punct_marks_ratio),
            ('exclamation_ratio', self._exclamation_ratio),
            ('question_ratio', self._question_ratio),
            ('caps_words_ratio', self._caps_words_ratio),
            ('caps_symbols_ratio', self._caps_symbols_ratio),
            ('stop_words_ratio', self._stop_words_ratio),
            ('unique_words_ratio', self._unique_words_ratio),
            ('positive_words_ratio', self._positive_words_ratio),
            ('negative_words_ratio', self._negative_words_ratio),
            ('intensifiers_ratio', self._intensifiers_ratio),
            ('diminishers_ratio', self._diminishers_ratio),
            ('bad_words_ratio', self._bad_words_ratio),
        ]

        features = [x[0] for x in features_functions]
        functions = [x[1] for x in features_functions]

        df['cleaned_text'] = df['text'].apply(lambda text: re.sub(r'[^a-zA-Zа-яА-ЯёЁ0-9]', ' ', text))
        df['lemmatized_text'] = df['cleaned_text'].apply(
            lambda text: ' '.join([self._lemmatize_word(word) for word in text.split()])
        )
        df[features] = df.apply(
            lambda row: [func(row.text, row.cleaned_text, row.lemmatized_text) for func in functions],
            axis=1, result_type='expand'
        )
        return df

    def _text_length(self, text: str, *args) -> int:
        """Длина текста"""
        return len(text)

    def _avg_word_length(self, text: str, *args) -> float:
        """Средняя длина слова"""
        return np.mean([len(x) for x in text.split()]).item()

    def _punct_marks_ratio(self, text: str, *args) -> float:
        """Доля знаков препинания"""
        punct_count = sum(text.count(mark) for mark in ',.;:!?\"\'')
        total_count = len(text)
        return punct_count / (total_count + 1e-10)

    def _exclamation_ratio(self, text: str, *args) -> float:
        """Доля восклицательных знаков"""
        exclamation_count = text.count('!')
        total_count = len(text)
        return exclamation_count / (total_count + 1e-10)

    def _question_ratio(self, text: str, *args) -> float:
        """Доля вопросительных знаков"""
        question_count = text.count('?')
        total_count = len(text)
        return question_count / (total_count + 1e-10)

    def _caps_words_ratio(self, text: str, cleaned_text: str, *args) -> float:
        """Доля слов в верхнем регистре"""
        words = cleaned_text.split()
        caps_words_count = sum(word.isupper() for word in words)
        words_total_count = len(words)
        return caps_words_count / (words_total_count + 1e-10)

    def _caps_symbols_ratio(self, text: str, cleaned_text: str, *args) -> float:
        """Доля символов в верхнем регистре"""
        caps_symbols_count = sum(x.isupper() for x in cleaned_text)
        total_count = len(cleaned_text)
        return caps_symbols_count / (total_count + 1e-10)

    def _unique_words_ratio(self, text: str, cleaned_text: str, lemmatized_text: str) -> float:
        """Доля уникальных слов"""
        unique_words_count = len(set(lemmatized_text.split()))
        total_words_count = len(lemmatized_text.split())
        return unique_words_count / (total_words_count + 1e-10)

    def _stop_words_ratio(self, text: str, cleaned_text: str, lemmatized_text: str) -> float:
        """Доля стоп-слов"""
        stop_words_count = sum(word in self.stop_words for word in lemmatized_text.split())
        total_words_count = len(lemmatized_text.split())
        return stop_words_count / (total_words_count + 1e-10)

    def _positive_words_ratio(self, text: str, cleaned_text: str, lemmatized_text: str) -> float:
        """Доля положительных слов"""
        positive_words_count = sum(word in self.positive_words for word in lemmatized_text.split())
        total_words_count = len(lemmatized_text.split())
        return positive_words_count / (total_words_count + 1e-10)

    def _negative_words_ratio(self, text: str, cleaned_text: str, lemmatized_text: str) -> float:
        """Доля отрицательных слов"""
        negative_words_count = sum(word in self.negative_words for word in lemmatized_text.split())
        total_words_count = len(lemmatized_text.split())
        return negative_words_count / (total_words_count + 1e-10)

    def _intensifiers_ratio(self, text: str, cleaned_text: str, lemmatized_text: str) -> float:
        """Доля слов-усилителей"""
        intensifier_words_count = sum(word in self.intensifiers for word in lemmatized_text.split())
        total_words_count = len(lemmatized_text.split())
        return intensifier_words_count / (total_words_count + 1e-10)

    def _diminishers_ratio(self, text: str, cleaned_text: str, lemmatized_text: str) -> float:
        """Доля слов-ослабителей"""
        diminisher_words_count = sum(word in self.diminishers for word in lemmatized_text.split())
        total_words_count = len(lemmatized_text.split())
        return diminisher_words_count / (total_words_count + 1e-10)

    def _bad_words_ratio(self, text: str, cleaned_text: str, lemmatized_text: str) -> float:
        """Доля плохих слов (матерных)"""
        bad_words_count = sum(word in self.bad_words for word in lemmatized_text.split())
        total_words_count = len(lemmatized_text.split())
        return bad_words_count / (total_words_count + 1e-10)