-- name: list-sales
-- Return ALL sales data
SELECT si.*, s.*, pc.category
  FROM saleitem si
       JOIN sale s ON si.sale = s.id
       JOIN product_category pc ON pc.product = si.product
ORDER BY s.purchase_date

-- name: list-sales-by-category
SELECT si.*, s.*
  FROM saleitem si
       JOIN sale s ON si.sale = s.id
       JOIN product_category pc ON (si.product = pc.product AND
                                    pc.category = :category)
ORDER BY s.purchase_date				    
       
