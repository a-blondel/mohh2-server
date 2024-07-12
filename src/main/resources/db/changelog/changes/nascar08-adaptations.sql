ALTER TABLE public.account ALTER COLUMN sku TYPE varchar(20) USING sku::varchar(20);
ALTER TABLE public.account ALTER COLUMN loc DROP NOT NULL;
ALTER TABLE public.account ALTER COLUMN tick DROP NOT NULL;
ALTER TABLE public.account ALTER COLUMN gamecode DROP NOT NULL;
