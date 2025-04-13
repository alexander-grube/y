CREATE TABLE public.roles
(
    id BIGSERIAL NOT NULL PRIMARY KEY,
    authority VARCHAR(255) NOT NULL UNIQUE
);

CREATE TABLE public.users
(
    id BIGSERIAL NOT NULL PRIMARY KEY,
    failed_login_attempts INT DEFAULT 0,
    created_at TIMESTAMPTZ NOT NULL DEFAULT CURRENT_TIMESTAMP,
    modified_at TIMESTAMPTZ NOT NULL DEFAULT CURRENT_TIMESTAMP,
    deleted_at TIMESTAMPTZ,
    last_login_at TIMESTAMPTZ,
    lock_until TIMESTAMPTZ,
    date_of_birth DATE NOT NULL,
    first_name VARCHAR(255) NOT NULL,
    last_name VARCHAR(255) NOT NULL,
    email VARCHAR(255) NOT NULL
        CHECK (email ~* '^[A-Za-z0-9._%-]+@[A-Za-z0-9.-]+[.][A-Za-z]+$'),
    phone VARCHAR(20),
    username VARCHAR(50) NOT NULL
        CHECK (LENGTH(username) >= 3),
    password VARCHAR(255) NOT NULL,
    created_by VARCHAR(255) NOT NULL,
    modified_by VARCHAR(255) NOT NULL,
    deleted_by VARCHAR(255),

    CONSTRAINT deletion_check CHECK (
            (deleted_at IS NULL AND deleted_by IS NULL) OR
            (deleted_at IS NOT NULL AND deleted_by IS NOT NULL)
    )
);

CREATE UNIQUE INDEX idx_users_email ON public.users (email)
    WHERE deleted_at IS NULL;

CREATE UNIQUE INDEX idx_users_username ON public.users (username)
    WHERE deleted_at IS NULL;

CREATE TABLE public.user_roles
(
    user_id BIGINT NOT NULL,
    role_id BIGINT NOT NULL,
    PRIMARY KEY (user_id, role_id),
    FOREIGN KEY (user_id) REFERENCES public.users (id) ON DELETE CASCADE,
    FOREIGN KEY (role_id) REFERENCES public.roles (id) ON DELETE CASCADE
);