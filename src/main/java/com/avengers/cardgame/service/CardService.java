package com.avengers.cardgame.service;

import com.avengers.cardgame.exception.UserNotFoundException;

public interface CardService {
    DailyRewardResult grantDaily(long userId) throws UserNotFoundException;
}
