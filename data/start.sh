#!/bin/bash

echo "=== Starting fetching Chess.com ==="
python -m collectors.by_club_chess_com
echo "=== Finished fetching Chess.com ==="

echo "=== Starting scraping Lichess ==="
python -m collectors.by_team_lichess
echo "=== Finished scraping Lichess ==="
