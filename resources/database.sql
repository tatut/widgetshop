
-- Category of products (like "Clothing")
CREATE TABLE category (
 id serial primary key,
 name varchar (128)
);


-- Product manufacturer company
CREATE TABLE manufacturer (
  id serial primary key,
  name varchar (128)
);

-- Very simple product listing
CREATE TABLE product (
 id serial primary key,
 name varchar (128),
 description text,
 price NUMERIC,
 manufacturer integer REFERENCES manufacturer (id)
);

-- Product can belong to multiple categories
CREATE TABLE product_category (
 product integer REFERENCES product (id),
 category integer REFERENCES category (id)
);

CREATE TABLE customer (
 id serial primary key,
 email varchar(255), -- email of customer
 name varchar(255) -- real name of customer
);

CREATE TABLE product_review (
 product integer REFERENCES product (id),
 customer integer REFERENCES customer (id),
 stars smallint, -- 1-5 star rating
 review text    -- text of the review
);


-- FIXME: product gallery

-- A single sale event
CREATE TABLE sale (
  id serial primary key,
  customer integer REFERENCES customer (id), -- who bought
  purchase_date timestamp -- when was the purchase made
);


-- Items in a sale event
CREATE TABLE saleitem (
  sale integer REFERENCES sale (id),
  product integer REFERENCES product (id),
  quantity integer,
  price NUMERIC -- total price paid 
);


---- TEST DATA

INSERT INTO manufacturer (name)
VALUES ('Acme Inc'),
       ('Blammo Toy Company'),
       ('Threepwood Pirate Clothing Inc'),
       ('Transgalactic Tools Ltd');

INSERT INTO product (name,description,price,manufacturer)
VALUES ('Acme earthquake pills', 'Why wait? Make your own earthquakes! Loads of fun.', 39.95, (SELECT id FROM manufacturer WHERE name='Acme Inc')),
       ('Fine leather jacket', 'I''m selling these fine leather jackets', 245, (SELECT id FROM manufacturer WHERE name='Threepwood Pirate Clothing Inc')),
       ('Log from Blammo!', 'It''s log, log, it''s big, it''s heavy, it''s wood. It''s log, log, it''s better than bad, it''s good.', 14.95, (SELECT id FROM manufacturer WHERE name='Blammo Toy Company')),
       ('Illudium Q-36 explosive space modulator', 'Planets obstructing YOUR view of Venus? Destroy them with the new explosive space modulator!', 20000, (SELECT id FROM manufacturer WHERE name='Transgalactic Tools Ltd'))
       ;
       
