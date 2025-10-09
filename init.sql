-- Boat Fuel Tracker Database Schema

USE boatfuel;

-- Users table
CREATE TABLE IF NOT EXISTS users (
    user_id VARCHAR(50) PRIMARY KEY,
    email VARCHAR(255) NOT NULL UNIQUE,
    password_hash VARCHAR(255),
    display_name VARCHAR(255),
    is_admin CHAR(1) DEFAULT 'N',
    created_at DATETIME DEFAULT CURRENT_TIMESTAMP,
    last_login DATETIME
) ENGINE=InnoDB DEFAULT CHARSET=utf8;

-- Fuel ups table
CREATE TABLE IF NOT EXISTS fuel_ups (
    fuel_up_id BIGINT AUTO_INCREMENT PRIMARY KEY,
    user_id VARCHAR(50) NOT NULL,
    fuel_date DATE NOT NULL,
    gallons DECIMAL(10,2) NOT NULL,
    price_per_gallon DECIMAL(10,2) NOT NULL,
    total_cost DECIMAL(10,2),
    engine_hours DECIMAL(10,1),
    location TEXT,
    notes TEXT,
    created_at DATETIME DEFAULT CURRENT_TIMESTAMP,
    FOREIGN KEY (user_id) REFERENCES users(user_id) ON DELETE CASCADE,
    INDEX idx_user_date (user_id, fuel_date),
    INDEX idx_fuel_date (fuel_date)
) ENGINE=InnoDB DEFAULT CHARSET=utf8;

-- Insert test user
INSERT INTO users (user_id, email, display_name, is_admin, created_at)
VALUES ('testuser', 'test@example.com', 'Test User', 'N', NOW())
ON DUPLICATE KEY UPDATE email=email;

-- Insert sample fuel-up data
INSERT INTO fuel_ups (user_id, fuel_date, gallons, price_per_gallon, total_cost, engine_hours, location, notes)
VALUES
    ('testuser', '2025-10-01', 25.5, 3.89, 99.20, 102.5, 'Marina Bay', 'Regular fuel-up'),
    ('testuser', '2025-09-15', 30.0, 3.95, 118.50, 95.2, 'Harbor Point', 'Long trip fuel-up'),
    ('testuser', '2025-09-01', 22.3, 3.79, 84.52, 88.1, 'Marina Bay', 'Weekend outing')
ON DUPLICATE KEY UPDATE fuel_up_id=fuel_up_id;
