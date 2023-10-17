
CREATE TABLE IF NOT EXISTS public.category
(
    id text NOT NULL,
    parent_category_ids text[],
    name text,
    created_at timestamp with time zone NOT NULL DEFAULT now(),
    modified_at timestamp with time zone NOT NULL DEFAULT now(),
    PRIMARY KEY (id)
);

CREATE TABLE IF NOT EXISTS public.category_path
(
    cat_id text NOT NULL,
    paths text[],
    created_at timestamp with time zone NOT NULL DEFAULT now(),
    modified_at timestamp with time zone NOT NULL DEFAULT now(),
    PRIMARY KEY (cat_id)
);

INSERT INTO CATEGORY (id, parent_category_ids, name, created_at, modified_at)
VALUES
(),
();


INSERT INTO CATEGORY_PATH (cat_id, paths, created_at, modified_at)
VALUES
(),
();
