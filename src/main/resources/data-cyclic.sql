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

insert into category(
id,
parent_category_ids
)
values
('cat12', '{"cat11", "cat6", "cat3"}'::text[]),
('cat7', '{"head"}'::text[]),
('cat3', '{"head"}'::text[]),
('cat2', '{"head"}'::text[]),
('cat4', '{"cat7", "cat9"}'::text[]),
('cat11', '{"cat4"}'::text[]),
('cat6', '{"cat4"}'::text[]),
('cat9', '{"cat7", "cat6"}'::text[])  -- NOTICE THAT THERE IS A CYCLIC REFERENCE HERE.
;