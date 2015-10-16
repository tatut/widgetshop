-- name: list-products-by-category
-- List all products for the given category
SELECT p.*,
       (SELECT AVG(stars)
          FROM product_review pr
	 WHERE pr.product = p.id) as average_rating
  FROM product p
       JOIN product_category pc ON (pc.product = p.id AND pc.category = :category)

-- name: list-products
-- List all products
SELECT p.*,
       (SELECT AVG(stars)
          FROM product_review pr
         WHERE pr.product = p.id) as average_rating
  FROM product p

-- name: list-product-reviews
-- List all reviews for a given product
SELECT pr.*,
       c.name as customer_name, c.email as customer_email
  FROM product_review pr
       JOIN customer c ON pr.customer = c.id
 WHERE pr.product = :p
      
-- name: create-customer<!
-- Create a customer
INSERT INTO CUSTOMER (name, email) VALUES (:name, :email)

-- name: create-product-review<!
-- Create product review
INSERT INTO product_review (product, customer, stars, review)
       VALUES (:product, :customer, :stars, :review)
