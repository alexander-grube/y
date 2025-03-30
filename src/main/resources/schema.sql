CREATE TABLE public.roles
(
    id BIGSERIAL NOT NULL PRIMARY KEY,
    authority VARCHAR(255) NOT NULL UNIQUE
);

CREATE TABLE public.users
(
    id BIGSERIAL NOT NULL PRIMARY KEY,
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    modified_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    deleted_at TIMESTAMP,
    date_of_birth TIMESTAMP NOT NULL,
    first_name VARCHAR(255) NOT NULL,
    last_name VARCHAR(255) NOT NULL,
    email VARCHAR(255) NOT NULL,
    phone VARCHAR(255),
    username VARCHAR(255) NOT NULL,
    password VARCHAR(255) NOT NULL,
    created_by VARCHAR(255) NOT NULL,
    modified_by VARCHAR(255) NOT NULL,
    deleted_by VARCHAR(255)
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