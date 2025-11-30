#!/bin/bash

echo "=== Starting fetching Chess.com ==="
python data/collectors/by_club_chess_com.py
echo "=== Finished fetching Chess.com ==="

echo "=== Starting scraping Lichess ==="
python data/collectors/by_team_lichess.py
echo "=== Finished scraping Lichess ==="
