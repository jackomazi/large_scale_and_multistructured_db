#!/bin/bash

echo "=== Starting fetching historical games from Kaggle ==="
python -m collectors.historical_games
echo "=== Finished fetching historical games from Kaggle ==="

echo "=== Starting fetching Chess.com ==="
python -m collectors.by_club_chess_com
echo "=== Finished fetching Chess.com ==="

echo "=== Starting scraping Lichess ==="
python -m collectors.load_lichess
echo "=== Finished scraping Lichess ==="
