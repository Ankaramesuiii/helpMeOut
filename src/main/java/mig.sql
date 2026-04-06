-- =============================================
-- ENUMS (immutable statuses)
-- =============================================

CREATE TYPE user_role_enum AS ENUM ('USER', 'ADMIN');
CREATE TYPE phone_verification_status_enum AS ENUM ('PENDING', 'VERIFIED', 'FAILED');
CREATE TYPE task_status_enum AS ENUM (
  'OPEN',                          -- Posted, waiting applications
  'HAS_APPLICATIONS',              -- At least one application
  'PROVIDER_SELECTED',             -- One provider chosen
  'IN_PROGRESS',                   -- Provider marked started
  'COMPLETION_PENDING',            -- Provider marked completed, waiting customer
  'COMPLETED',                     -- Customer confirmed completion
  'PAYMENT_PENDING',               -- Completed, waiting payment confirmations
  'PAID',                          -- Both confirmed payment
  'CANCELLED',                     -- Cancelled before start
  'EXPIRED'                        -- Auto-complete after 1 month timeout
);
CREATE TYPE application_status_enum AS ENUM ('PENDING', 'SELECTED', 'REJECTED', 'WITHDRAWN');
CREATE TYPE chat_status_enum AS ENUM ('ACTIVE', 'CLOSED');
CREATE TYPE availability_day_enum AS ENUM ('SATURDAY', 'SUNDAY', 'MONDAY', 'TUESDAY', 'WEDNESDAY', 'THURSDAY', 'FRIDAY');
CREATE TYPE scheduled_type_enum AS ENUM ('ASAP', 'SCHEDULED');

-- =============================================
-- CORE TABLES
-- =============================================

-- 1. Users (one account for customer + provider)
CREATE TABLE users (
    id BIGSERIAL PRIMARY KEY,
    phone VARCHAR(20) UNIQUE NOT NULL,  -- Tunisian format: +216xxxxxxxxx
    password_hash VARCHAR(255) NOT NULL,
    role user_role_enum NOT NULL DEFAULT 'USER',
    phone_verification_status phone_verification_status_enum DEFAULT 'PENDING',
    is_active BOOLEAN DEFAULT TRUE,
    created_at TIMESTAMP WITH TIME ZONE DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP WITH TIME ZONE DEFAULT CURRENT_TIMESTAMP,
    deleted_at TIMESTAMP WITH TIME ZONE NULL
);

-- 2. User profiles (basic for all users, visible to others)
CREATE TABLE user_profiles (
    id BIGSERIAL PRIMARY KEY,
    user_id BIGINT UNIQUE NOT NULL REFERENCES users(id) ON DELETE CASCADE,
    full_name VARCHAR(255) NOT NULL,     -- Arabic support
    profile_photo_url VARCHAR(500),
    city VARCHAR(100) NOT NULL,          -- e.g., "تونس"
    area VARCHAR(100),                   -- e.g., "اللاسيل"
    bio TEXT,
    customer_rating DECIMAL(3,2) DEFAULT 0,  -- 0-5
    customer_completed_tasks INTEGER DEFAULT 0,
    created_at TIMESTAMP WITH TIME ZONE DEFAULT CURRENT_TIMESTAMP
);

-- 3. Service categories (fixed for MVP)
CREATE TABLE categories (
    id BIGSERIAL PRIMARY KEY,
    name_ar VARCHAR(100) NOT NULL UNIQUE,  -- "تركيب", etc.
    slug VARCHAR(100) UNIQUE NOT NULL,      -- "tarikib"
    is_active BOOLEAN DEFAULT TRUE,
    form_config JSONB DEFAULT '{}',         -- Category-specific fields, e.g. {"needs_vehicle": true}
    created_at TIMESTAMP WITH TIME ZONE DEFAULT CURRENT_TIMESTAMP
);

-- Insert your MVP categories
INSERT INTO categories (name_ar, slug, form_config) VALUES
('تركيب', 'tarikib', '{"fields": ["item_type", "has_tools"]}'::JSONB),
('توصيل', 'taswil', '{"fields": ["pickup_location", "dropoff_distance"]}'::JSONB),
('نقل / حمل', 'naql_hamal', '{"fields": ["item_weight", "stairs"]}'::JSONB),
('تنظيف', 'tanzeef', '{"fields": ["room_size", "recurring"]}'::JSONB),
('إصلاحات بسيطة', 'islahat_basita', '{"fields": ["problem_description"]}'::JSONB),
('مساعدة منزلية', 'masada_manziliya', '{}');

-- 4. Provider profiles (required to apply)
CREATE TABLE provider_profiles (
    id BIGSERIAL PRIMARY KEY,
    user_id BIGINT UNIQUE NOT NULL REFERENCES users(id) ON DELETE CASCADE,
    description TEXT,
    provider_rating DECIMAL(3,2) DEFAULT 0,
    completed_tasks_count INTEGER DEFAULT 0,
    is_active BOOLEAN DEFAULT TRUE,
    created_at TIMESTAMP WITH TIME ZONE DEFAULT CURRENT_TIMESTAMP
);

-- 5. Provider categories (many-to-many)
CREATE TABLE provider_categories (
    provider_profile_id BIGINT NOT NULL REFERENCES provider_profiles(id) ON DELETE CASCADE,
    category_id BIGINT NOT NULL REFERENCES categories(id) ON DELETE CASCADE,
    PRIMARY KEY (provider_profile_id, category_id)
);

-- 6. Provider service areas (many-to-many like)
CREATE TABLE provider_service_areas (
    id BIGSERIAL PRIMARY KEY,
    provider_profile_id BIGINT NOT NULL REFERENCES provider_profiles(id) ON DELETE CASCADE,
    city VARCHAR(100) NOT NULL,
    area VARCHAR(100),
    radius_km INTEGER DEFAULT 10  -- Optional radius
);

-- 7. Provider availability
CREATE TABLE provider_availability (
    id BIGSERIAL PRIMARY KEY,
    provider_profile_id BIGINT NOT NULL REFERENCES provider_profiles(id) ON DELETE CASCADE,
    day availability_day_enum NOT NULL,
    start_time TIME NOT NULL,
    end_time TIME NOT NULL,
    is_active BOOLEAN DEFAULT TRUE
);

-- 8. Tasks (core marketplace entity)
CREATE TABLE tasks (
    id BIGSERIAL PRIMARY KEY,
    customer_id BIGINT NOT NULL REFERENCES users(id) ON DELETE CASCADE,
    category_id BIGINT NOT NULL REFERENCES categories(id),
    title VARCHAR(255) NOT NULL,
    description TEXT NOT NULL,
    budget_tnd DECIMAL(10,2) NOT NULL,
    city VARCHAR(100) NOT NULL,
    area VARCHAR(100),
    latitude DECIMAL(10,8),      -- Exact pin (hidden until selected)
    longitude DECIMAL(11,8),
    exact_location_visible BOOLEAN DEFAULT FALSE,
    category_specific_data JSONB DEFAULT '{}',  -- e.g. {"item_weight": "50kg"}
    scheduled_type scheduled_type_enum DEFAULT 'ASAP',
    scheduled_at TIMESTAMP WITH TIME ZONE,
    status task_status_enum NOT NULL DEFAULT 'OPEN',
    selected_provider_id BIGINT REFERENCES provider_profiles(id),
    created_at TIMESTAMP WITH TIME ZONE DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP WITH TIME ZONE DEFAULT CURRENT_TIMESTAMP
);

-- 9. Task photos
CREATE TABLE task_photos (
    id BIGSERIAL PRIMARY KEY,
    task_id BIGINT NOT NULL REFERENCES tasks(id) ON DELETE CASCADE,
    image_url VARCHAR(500) NOT NULL,
    is_primary BOOLEAN DEFAULT FALSE,
    created_at TIMESTAMP WITH TIME ZONE DEFAULT CURRENT_TIMESTAMP
);

-- 10. Task applications
CREATE TABLE task_applications (
    id BIGSERIAL PRIMARY KEY,
    task_id BIGINT NOT NULL REFERENCES tasks(id) ON DELETE CASCADE,
    provider_profile_id BIGINT NOT NULL REFERENCES provider_profiles(id) ON DELETE CASCADE,
    proposed_price DECIMAL(10,2),
    application_message TEXT,
    status application_status_enum DEFAULT 'PENDING',
    created_at TIMESTAMP WITH TIME ZONE DEFAULT CURRENT_TIMESTAMP
);

-- 11. Chats (one per application/task)
CREATE TABLE chats (
    id BIGSERIAL PRIMARY KEY,
    task_id BIGINT NOT NULL REFERENCES tasks(id) ON DELETE CASCADE,
    task_application_id BIGINT REFERENCES task_applications(id) ON DELETE CASCADE,
    customer_id BIGINT NOT NULL REFERENCES users(id) ON DELETE CASCADE,
    provider_profile_id BIGINT NOT NULL REFERENCES provider_profiles(id) ON DELETE CASCADE,
    status chat_status_enum DEFAULT 'ACTIVE',
    created_at TIMESTAMP WITH TIME ZONE DEFAULT CURRENT_TIMESTAMP
);

-- 12. Chat messages
CREATE TABLE chat_messages (
    id BIGSERIAL PRIMARY KEY,
    chat_id BIGINT NOT NULL REFERENCES chats(id) ON DELETE CASCADE,
    sender_id BIGINT NOT NULL REFERENCES users(id) ON DELETE CASCADE,
    message_type VARCHAR(20) DEFAULT 'TEXT',  -- TEXT, IMAGE
    content TEXT NOT NULL,
    read_at TIMESTAMP WITH TIME ZONE NULL,
    created_at TIMESTAMP WITH TIME ZONE DEFAULT CURRENT_TIMESTAMP
);

-- 13. Reviews (bidirectional: customer->provider or provider->customer)
CREATE TABLE reviews (
    id BIGSERIAL PRIMARY KEY,
    task_id BIGINT NOT NULL REFERENCES tasks(id) ON DELETE CASCADE,
    reviewer_id BIGINT NOT NULL REFERENCES users(id) ON DELETE CASCADE,
    reviewee_id BIGINT NOT NULL REFERENCES users(id) ON DELETE CASCADE,  -- Provider or customer
    rating INTEGER CHECK (rating >= 1 AND rating <= 5) NOT NULL,
    comment TEXT,
    created_at TIMESTAMP WITH TIME ZONE DEFAULT CURRENT_TIMESTAMP
);

-- 14. Task status history (audit trail)
CREATE TABLE task_status_history (
    id BIGSERIAL PRIMARY KEY,
    task_id BIGINT NOT NULL REFERENCES tasks(id) ON DELETE CASCADE,
    old_status task_status_enum,
    new_status task_status_enum NOT NULL,
    changed_by_user_id BIGINT REFERENCES users(id),
    reason TEXT,
    created_at TIMESTAMP WITH TIME ZONE DEFAULT CURRENT_TIMESTAMP
);

-- 15. Payment confirmations (cash tracking)
CREATE TABLE payment_confirmations (
    id BIGSERIAL PRIMARY KEY,
    task_id BIGINT UNIQUE NOT NULL REFERENCES tasks(id) ON DELETE CASCADE,
    customer_paid BOOLEAN DEFAULT FALSE,
    customer_paid_at TIMESTAMP WITH TIME ZONE NULL,
    provider_received BOOLEAN DEFAULT FALSE,
    provider_confirmed_at TIMESTAMP WITH TIME ZONE NULL,
    created_at TIMESTAMP WITH TIME ZONE DEFAULT CURRENT_TIMESTAMP
);

-- 16. Notifications (in-app)
CREATE TABLE notifications (
    id BIGSERIAL PRIMARY KEY,
    user_id BIGINT NOT NULL REFERENCES users(id) ON DELETE CASCADE,
    type VARCHAR(50) NOT NULL,  -- NEW_APPLICATION, MESSAGE, etc.
    title_ar VARCHAR(255) NOT NULL,
    body_ar TEXT,
    is_read BOOLEAN DEFAULT FALSE,
    data JSONB DEFAULT '{}',
    created_at TIMESTAMP WITH TIME ZONE DEFAULT CURRENT_TIMESTAMP
);

-- =============================================
-- INDEXES (performance critical)
-- =============================================

-- Users
CREATE INDEX idx_users_phone ON users(phone);
CREATE INDEX idx_users_phone_verified ON users(phone_verification_status);

-- Tasks
CREATE INDEX idx_tasks_status ON tasks(status);
CREATE INDEX idx_tasks_category ON tasks(category_id);
CREATE INDEX idx_tasks_city_area ON tasks(city, area);
CREATE INDEX idx_tasks_customer ON tasks(customer_id);
CREATE INDEX idx_tasks_scheduled ON tasks(scheduled_at);
CREATE INDEX idx_tasks_location ON tasks(latitude, longitude);  -- For geo queries later

-- Applications
CREATE INDEX idx_applications_task ON task_applications(task_id);
CREATE INDEX idx_applications_provider ON task_applications(provider_profile_id);
CREATE INDEX idx_applications_status ON task_applications(status);

-- Chat
CREATE INDEX idx_chat_messages_chat ON chat_messages(chat_id);
CREATE INDEX idx_chat_messages_read ON chat_messages(chat_id, read_at);
CREATE INDEX idx_chat_messages_sender ON chat_messages(sender_id);

-- Notifications
CREATE INDEX idx_notifications_user ON notifications(user_id);
CREATE INDEX idx_notifications_read ON notifications(user_id, is_read);

-- Reviews
CREATE INDEX idx_reviews_task ON reviews(task_id);
CREATE INDEX idx_reviews_reviewee ON reviews(reviewee_id);

-- Provider
CREATE INDEX idx_provider_categories ON provider_categories(category_id);
CREATE INDEX idx_provider_service_areas ON provider_service_areas(city, area);

-- =============================================
-- TRIGGERS / FUNCTIONS (automation)
-- =============================================

-- Auto-update updated_at
CREATE OR REPLACE FUNCTION update_updated_at_column()
RETURNS TRIGGER AS $$
BEGIN
    NEW.updated_at = CURRENT_TIMESTAMP;
    RETURN NEW;
END;
$$ language 'plpgsql';

CREATE TRIGGER update_users_updated_at BEFORE UPDATE ON users FOR EACH ROW EXECUTE PROCEDURE update_updated_at_column();
CREATE TRIGGER update_tasks_updated_at BEFORE UPDATE ON tasks FOR EACH ROW EXECUTE PROCEDURE update_updated_at_column();

-- Auto-log status changes
CREATE OR REPLACE FUNCTION log_task_status_change()
RETURNS TRIGGER AS $$
BEGIN
    IF OLD.status != NEW.status THEN
        INSERT INTO task_status_history (task_id, old_status, new_status, changed_by_user_id)
        VALUES (NEW.id, OLD.status, NEW.status, NULL);  -- Later: get from session
    END IF;
    RETURN NEW;
END;
$$ language 'plpgsql';

CREATE TRIGGER task_status_log BEFORE UPDATE ON tasks
FOR EACH ROW WHEN (OLD.status IS DISTINCT FROM NEW.status)
EXECUTE PROCEDURE log_task_status_change();

-- =============================================
-- SAMPLE DATA / TESTS
-- =============================================

-- Enable extensions for full-text search later (Arabic support)
CREATE EXTENSION IF NOT EXISTS unaccent;
CREATE EXTENSION IF NOT EXISTS pg_trgm;  -- For fuzzy search on Arabic names