-- name: list-products-by-category
-- List all products for the given category
SELECT * FROM product WHERE category = :category
