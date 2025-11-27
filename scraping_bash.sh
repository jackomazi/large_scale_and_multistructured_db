#!/bin/bash

echo "=== Starting scraping Chess.com ==="

python Python_scraping/chess_com_scraping_based_on_club_names.py
echo "=== Finished scraping Chess.com ==="

echo "=== Starting scraping Lichess ==="
python Python_scraping/lichess_scraping_based_on_team_names.py
echo "=== Finished scraping Lichess ==="