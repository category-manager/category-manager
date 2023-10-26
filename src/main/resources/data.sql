
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

CREATE TABLE IF NOT EXISTS public.category_v2
(
    id text NOT NULL,
    parent_category_ids text[],
    name text,
    created_at timestamp with time zone NOT NULL DEFAULT now(),
    modified_at timestamp with time zone NOT NULL DEFAULT now(),
    PRIMARY KEY (id)
);

-- project structure data
insert into category( id, parent_category_ids)
values
('category-manager', '{"head"}'::text[]),
('core', '{"category-manager"}'::text[]),
('clientService', '{"core"}'::text[]),
('pojo', '{"core"}'::text[]),
('sp', '{"core"}'::text[]),
('spi', '{"core"}'::text[]),
('examples', '{"core"}'::text[]),
('categoryManagerClient.java', '{"clientService"}'),
('Categories.java', '{"pojo"}'::text[]),
('CategoriesPaths.java', '{"pojo"}'::text[]),
('CoreConstants.java', '{"pojo"}'::text[]),
('DefaultCategoryExportData.java', '{"pojo"}'::text[]),
('DefaultCategoryPathExportData.java', '{"pojo"}'::text[]),
('NodePresence.java', '{"pojo"}'::text[]),
('PathResponse.java', '{"pojo"}'::text[]),
('DefaultData.java', '{"sp"}'::text[]),
('DefaultDbExportImpl.java', '{"sp"}'::text[]),
('DefaultImportImpl.java', '{"sp"}'::text[]),
('DefaultRealtimeOperationImpl.java	', '{"sp"}'::text[]),
('DefaultRestExportImpl.java', '{"sp"}'::text[]),
('Node.java', '{"sp"}'::text[]),
('Utility.java', '{"sp"}'::text[]),
('Data.java', '{"spi"}'::text[]),
('Export.java', '{"spi"}'::text[]),
('ICategories.java', '{"spi"}'::text[]),
('ICategoriesPaths.java', '{"spi"}'::text[]),
('ICategoryExportData.java', '{"spi"}'::text[]),
('ICategoryPathExportData.java', '{"spi"}'::text[]),
('Import.java', '{"spi"}'::text[]),
('RealtimeOperation.java', '{"spi"}'::text[]),
('TestDbData.java', '{"examples"}'::text[]),
('TestServiceInMemory.java', '{"examples"}'::text[])
;
