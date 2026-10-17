<?xml version="1.0" encoding="UTF-8"?>
-- Schema for dear_freshman recreated from IDE introspection
-- Use InnoDB, utf8mb4 charset

CREATE DATABASE IF NOT EXISTS dear_freshman CHARACTER SET = utf8mb4 COLLATE = utf8mb4_general_ci;
USE dear_freshman;

-- players table
CREATE TABLE IF NOT EXISTS players (
  player_id INT AUTO_INCREMENT PRIMARY KEY,
  player_name VARCHAR(100) NOT NULL,
  created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP NOT NULL
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_general_ci;

-- game_saves table
CREATE TABLE IF NOT EXISTS game_saves (
  save_id INT AUTO_INCREMENT PRIMARY KEY,
  player_id INT NOT NULL,
  save_name VARCHAR(100) DEFAULT 'Save 1',
  current_chapter INT DEFAULT 1,
  current_scene VARCHAR(100) DEFAULT 'start',
  game_day INT DEFAULT 1,
  game_minutes INT DEFAULT 480,
  updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP NOT NULL ON UPDATE CURRENT_TIMESTAMP,
  current_event_index INT NOT NULL DEFAULT 0,
  CONSTRAINT fk_game_saves_player FOREIGN KEY (player_id) REFERENCES players(player_id) ON DELETE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_general_ci;

-- player_stats table
CREATE TABLE IF NOT EXISTS player_stats (
  save_id INT NOT NULL PRIMARY KEY,
  energy INT DEFAULT 100,
  friendship INT DEFAULT 0,
  reputation INT DEFAULT 50,
  academics INT DEFAULT 25,
  suspicion INT DEFAULT 0,
  gino_trust INT DEFAULT 50,
  z_trust INT DEFAULT 50,
  obet_trust INT DEFAULT 50,
  william_suspicion INT DEFAULT 0,
  programming_score INT DEFAULT 0,
  database_score INT DEFAULT 0,
  history_score INT DEFAULT 0,
  quiz_answered INT DEFAULT 0,
  CONSTRAINT fk_player_stats_save FOREIGN KEY (save_id) REFERENCES game_saves(save_id) ON DELETE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_general_ci;

-- choice_history table
CREATE TABLE IF NOT EXISTS choice_history (
  decision_id INT AUTO_INCREMENT PRIMARY KEY,
  save_id INT NOT NULL,
  chapter INT NOT NULL,
  event_id VARCHAR(100) NOT NULL,
  choice_id VARCHAR(100) NOT NULL,
  choice_text TEXT NOT NULL,
  selected_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP NOT NULL,
  INDEX idx_choice_history_save (save_id),
  CONSTRAINT fk_choice_history_save FOREIGN KEY (save_id) REFERENCES game_saves(save_id) ON DELETE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_general_ci;

-- character_relationships table
CREATE TABLE IF NOT EXISTS character_relationships (
  save_id INT NOT NULL,
  character_id VARCHAR(100) NOT NULL,
  relationship_value INT DEFAULT 0,
  PRIMARY KEY (save_id, character_id),
  CONSTRAINT fk_character_relationships_save FOREIGN KEY (save_id) REFERENCES game_saves(save_id) ON DELETE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_general_ci;

-- story_flags table
CREATE TABLE IF NOT EXISTS story_flags (
  save_id INT NOT NULL,
  flag_name VARCHAR(100) NOT NULL,
  flag_value VARCHAR(255) NOT NULL,
  PRIMARY KEY (save_id, flag_name),
  CONSTRAINT fk_story_flags_save FOREIGN KEY (save_id) REFERENCES game_saves(save_id) ON DELETE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_general_ci;
