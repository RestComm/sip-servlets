delete from ORDERLINES
delete from ORDERS
delete from USERS
delete from INVENTORY
delete from PRODUCT_CATEGORY
delete from PRODUCT_ACTORS
delete from ACTORS
delete from PRODUCTS
delete from CATEGORIES

INSERT INTO USERS (USERID,DTYPE,USERNAME,PASSWORD,FIRSTNAME,LASTNAME) VALUES (1,'admin','manager','password','Albus', 'Dumblebore')
INSERT INTO USERS (USERID,DTYPE,FIRSTNAME,LASTNAME,ADDRESS1,ADDRESS2,CITY,STATE,ZIP,EMAIL,PHONE,CREDITCARDTYPE,CC_NUM,CC_MONTH,CC_YEAR,USERNAME,PASSWORD) VALUES (2,'customer','Harry','Potter','4 Privet Drive','Cupboard under the Stairs','QSDPAGD','SD',24101,'h.potter@hogwarts.edu','sip:abhayani@192.168.0.100:5059',1,'1979279217775911',03,2012,'user1','password')
INSERT INTO USERS (USERID,DTYPE,FIRSTNAME,LASTNAME,ADDRESS1,ADDRESS2,CITY,STATE,ZIP,EMAIL,PHONE,CREDITCARDTYPE,CC_NUM,CC_MONTH,CC_YEAR,USERNAME,PASSWORD) VALUES (3,'customer','Hermione','Granger','5119315633 Dell Way','','YNCERXJ','AZ',11802,'h.granger@hogwarts.edu','sip:abhayani@127.0.0.1:5059',1,'3144519586581737',11,2012,'user2','password')
INSERT INTO USERS (USERID,DTYPE,FIRSTNAME,LASTNAME,ADDRESS1,ADDRESS2,CITY,STATE,ZIP,EMAIL,PHONE,CREDITCARDTYPE,CC_NUM,CC_MONTH,CC_YEAR,USERNAME,PASSWORD) VALUES (4,'customer','Ron','Weasley','6297761196 Dell Way','','LWVIFXJ','OH',96082,'r.weasley@hogwarts.edu','sip:+919960639901@callwithus.com',4,'8728086929768325',12,2010,'user3','password')
INSERT INTO USERS (USERID,DTYPE,FIRSTNAME,LASTNAME,ADDRESS1,ADDRESS2,CITY,STATE,ZIP,EMAIL,PHONE,CREDITCARDTYPE,CC_NUM,CC_MONTH,CC_YEAR,USERNAME,PASSWORD) VALUES (5,'customer','Sanjeewa','Vijayratna','9862764981 Dell Way','','HOKEXCD','MS',78442,'n.longbottom@hogwarts.edu','sip:+94773123543@callwithus.com',5,'7160005148965866',09,2009,'user4','password')
INSERT INTO USERS (USERID,DTYPE,FIRSTNAME,LASTNAME,ADDRESS1,ADDRESS2,CITY,STATE,ZIP,EMAIL,PHONE,CREDITCARDTYPE,CC_NUM,CC_MONTH,CC_YEAR,USERNAME,PASSWORD) VALUES (6,'customer','Ginny','Weasley','2841895775 Dell Way','','RZQTCDN','AZ',16291,'g.weasley@hogwarts.edu','2841895775',3,'8377095518168063',10,2010,'user5','password')
INSERT INTO USERS (USERID,DTYPE,FIRSTNAME,LASTNAME,ADDRESS1,ADDRESS2,CITY,STATE,ZIP,EMAIL,PHONE,CREDITCARDTYPE,CC_NUM,CC_MONTH,CC_YEAR,USERNAME,PASSWORD) VALUES (7,'customer','Jean','Deruelle','69 highway to heaven','','Nowhere','MD',66666,'jean.deruelle@gmail.com','sip:dereuelle@shopping-demo.com',1,'8377095518168063',10,2010,'jean','password')
INSERT INTO USERS (USERID,DTYPE,FIRSTNAME,LASTNAME,ADDRESS1,ADDRESS2,CITY,STATE,ZIP,EMAIL,PHONE,CREDITCARDTYPE,CC_NUM,CC_MONTH,CC_YEAR,USERNAME,PASSWORD) VALUES (8,'customer','Jean','Deruelle','69 highway to heaven','','Nowhere','MD',66666,'jean.deruelle@gmail.com','sip:deruelle@127.0.0.1:5090',1,'8377095518168063',10,2010,'deruelle','password')
INSERT INTO USERS (USERID,DTYPE,FIRSTNAME,LASTNAME,ADDRESS1,ADDRESS2,CITY,STATE,ZIP,EMAIL,PHONE,CREDITCARDTYPE,CC_NUM,CC_MONTH,CC_YEAR,USERNAME,PASSWORD) VALUES (9,'customer','Jean','Deruelle','6297761196 Dell Way','','LWVIFXJ','OH',96082,'r.weasley@hogwarts.edu','sip:+33951385310@callwithus.com',4,'8728086929768325',12,2010,'jeand','password')

INSERT INTO USERS (USERID,DTYPE,FIRSTNAME,LASTNAME,ADDRESS1,ADDRESS2,CITY,STATE,ZIP,EMAIL,PHONE,CREDITCARDTYPE,CC_NUM,CC_MONTH,CC_YEAR,USERNAME,PASSWORD) VALUES (10,'customer','Ivelin','Ivanov','6297761196 Dell Way','','LWVIFXJ','OH',96082,'r.weasley@hogwarts.edu','sip:+15129703297@callwithus.com',4,'8728086929768325',10,2010,'ivelin','password')

INSERT INTO USERS (USERID,DTYPE,FIRSTNAME,LASTNAME,ADDRESS1,ADDRESS2,CITY,STATE,ZIP,EMAIL,PHONE,CREDITCARDTYPE,CC_NUM,CC_MONTH,CC_YEAR,USERNAME,PASSWORD) VALUES (11,'customer','Jean','Deruelle','6297761196 Dell Way','','LWVIFXJ','OH',96082,'r.weasley@hogwarts.edu','sip:+14153427704@callwithus.com',4,'8728086929768325',10,2010,'mobile','password')

INSERT INTO USERS (USERID,DTYPE,FIRSTNAME,LASTNAME,ADDRESS1,ADDRESS2,CITY,STATE,ZIP,EMAIL,PHONE,CREDITCARDTYPE,CC_NUM,CC_MONTH,CC_YEAR,USERNAME,PASSWORD) VALUES (12,'customer','Jean','Deruelle','6297761196 Dell Way','','LWVIFXJ','OH',96082,'r.weasley@hogwarts.edu','sip:+33679364376@callwithus.com',4,'8728086929768325',10,2010,'mobile-fr','password')

INSERT INTO USERS (USERID,DTYPE,FIRSTNAME,LASTNAME,ADDRESS1,ADDRESS2,CITY,STATE,ZIP,EMAIL,PHONE,CREDITCARDTYPE,CC_NUM,CC_MONTH,CC_YEAR,USERNAME,PASSWORD) VALUES (13,'customer','Jean','Deruelle','6297761196 Dell Way','','LWVIFXJ','OH',96082,'r.weasley@hogwarts.edu','sip:+33327861445@callwithus.com',4,'8728086929768325',12,2010,'home','password')


insert into ACTORS (ID, NAME) values (1, 'Tom Hanks')
insert into ACTORS (ID, NAME) values (2, 'Katie Holmes')
insert into ACTORS (ID, NAME) values (3, 'Drew Barrymore')
insert into ACTORS (ID, NAME) values (4, 'Daniel Radcliffe')
insert into ACTORS (ID, NAME) values (5, 'Jim Carrey')
insert into ACTORS (ID, NAME) values (6, 'Scarlett Johansson')
insert into ACTORS (ID, NAME) values (7, 'Bill Murray')
insert into ACTORS (ID, NAME) values (8, 'Owen Wilson')
insert into ACTORS (ID, NAME) values (9, 'Luke Wilson')
insert into ACTORS (ID, NAME) values (10, 'Tobey Maguire')
insert into ACTORS (ID, NAME) values (11, 'John Cusak')
insert into ACTORS (ID, NAME) values (12, 'Jack Black')
insert into ACTORS (ID, NAME) values (13, 'Keanu Reeves')
insert into ACTORS (ID, NAME) values (14, 'Christopher Reeve')
insert into ACTORS (ID, NAME) values (15, 'Harrison Ford')
insert into ACTORS (ID, NAME) values (16, 'Kirsten Dunst')
insert into ACTORS (ID, NAME) values (17, 'Elijah Wood')
insert into ACTORS (ID, NAME) values (18, 'Laurence Fishburne')
insert into ACTORS (ID, NAME) values (19, 'Meg Ryan')
insert into ACTORS (ID, NAME) values (20, 'Billy Crystal')
insert into ACTORS (ID, NAME) values (21, 'Wesley Snipes')
insert into ACTORS (ID, NAME) values (22, 'Ewan McGregor')
insert into ACTORS (ID, NAME) values (23, 'Natalie Portman')
insert into ACTORS (ID, NAME) values (24, 'Jon Heder')
insert into ACTORS (ID, NAME) values (25, 'Vince Vaughn')
insert into ACTORS (ID, NAME) values (26, 'Ben Stiller')
insert into ACTORS (ID, NAME) values (27, 'Matt Damon')
insert into ACTORS (ID, NAME) values (28, 'Jodie Foster')
insert into ACTORS (ID, NAME) values (29, 'Matthew McConaughey')
insert into ACTORS (ID, NAME) values (30, 'Ed Harris')
insert into ACTORS (ID, NAME) values (31, 'Ralph Fiennes')
insert into ACTORS (ID, NAME) values (32, 'Gwyneth Paltrow')
insert into ACTORS (ID, NAME) values (33, 'Brad Pitt')
insert into ACTORS (ID, NAME) values (34, 'Angelina Jolie')
insert into ACTORS (ID, NAME) values (35, 'Edward Norton')
insert into ACTORS (ID, NAME) values (36, 'Adam Sandler')
insert into ACTORS (ID, NAME) values (37, 'Johnny Depp')
insert into ACTORS (ID, NAME) values (38, 'Keira Knightley')
insert into ACTORS (ID, NAME) values (39, 'Robin Williams')
insert into ACTORS (ID, NAME) values (40, 'Tom Cruise')
insert into ACTORS (ID, NAME) values (41, 'Bruce Willis')
insert into ACTORS (ID, NAME) values (42, 'Patrick Stewart')
insert into ACTORS (ID, NAME) values (43, 'Halle Berry')
insert into ACTORS (ID, NAME) values (44, 'Jennifer Aniston')
insert into ACTORS (ID, NAME) values (45, 'Julia Stiles')
insert into ACTORS (ID, NAME) values (46, 'Winona Ryder')
insert into ACTORS (ID, NAME) values (47, 'Kate Hudson')
insert into ACTORS (ID, NAME) values (48, 'Uma Thurman')
insert into ACTORS (ID, NAME) values (49, 'Julia Roberts')
insert into ACTORS (ID, NAME) values (50, 'Steve Carell')
insert into ACTORS (ID, NAME) values (51, 'Catherine Keener')
insert into ACTORS (ID, NAME) values (52, 'Franka Potente')
insert into ACTORS (ID, NAME) values (53, 'Catherine Zeta-Jones')
insert into ACTORS (ID, NAME) values (54, 'Tim Robbins')
insert into ACTORS (ID, NAME) values (55, 'Cate Blanchett')
insert into ACTORS (ID, NAME) values (56, 'Orlando Bloom')
insert into ACTORS (ID, NAME) values (57, 'Liv Tyler')
insert into ACTORS (ID, NAME) values (58, 'Ben Affleck')
insert into ACTORS (ID, NAME) values (59, 'Jack Nicholson')
insert into ACTORS (ID, NAME) values (60, 'Meryl Streep')
insert into ACTORS (ID, NAME) values (61, 'John Travolta')
insert into ACTORS (ID, NAME) values (62, 'Cary Grant')
insert into ACTORS (ID, NAME) values (63, 'Woody Allen')
insert into ACTORS (ID, NAME) values (64, 'Will Smith')
insert into ACTORS (ID, NAME) values (65, 'Sean Connery')
insert into ACTORS (ID, NAME) values (66, 'Kevin Costner')
insert into ACTORS (ID, NAME) values (67, 'Arnold Schwarzenegger')
insert into ACTORS (ID, NAME) values (68, 'Audrey Hepburn')
insert into PRODUCTS (PROD_ID, ASIN, TITLE, PRICE, IMAGE_URL, DESCRIPTION) values ('1', '11fj-v0siZL._AA160_', 'Global Distinctions Buy-1-Get-1-Free Inside Ottoman, Brown', 149.99, './img/ottoman.jpg', 'A comfortable, yet stylish addition to any home, this two-for-one ottoman set features one large square-shaped storage ottoman plus one square ottoman that fits inside. Both pieces feature a durable frame and thickly padded tops and sides. Tuck them under a table when not in use, and pull them out to serve as additional seating when needed. They can also function as footrests or bedside tables. The versatile storage ottoman''s top removes easily to reveal convenient storage space inside--great for storing an extra blanket, magazines, or books. The underside of the removable top serves as a wooden tray table when eating or playing games. The storage ottoman measures approximately 15 by 15 by 18 inches. ');
insert into PRODUCT_ACTORS (PROD_ID,ACTOR_ID) values (1, 68)
insert into CATEGORIES (CATEGORY, NAME) values (1, 'Living Room');
insert into PRODUCT_CATEGORY (PROD_ID, CATEGORY) values (1, 1);
insert into CATEGORIES (CATEGORY, NAME) values (2, 'Bedroom');
insert into PRODUCT_CATEGORY (PROD_ID, CATEGORY) values (1, 2);
insert into CATEGORIES (CATEGORY, NAME) values (3, 'Home Décor');
insert into PRODUCT_CATEGORY (PROD_ID, CATEGORY) values (1, 3);
insert into CATEGORIES (CATEGORY, NAME) values (4, 'Home Office');
insert into PRODUCT_CATEGORY (PROD_ID, CATEGORY) values (1, 4);
insert into CATEGORIES (CATEGORY, NAME) values (5, 'Lighting');
insert into CATEGORIES (CATEGORY, NAME) values (6, 'Rugs');
insert into INVENTORY (INV_ID, PROD_ID, QUAN_IN_STOCK, SALES) values (1, 1, 84, 33);

insert into PRODUCTS (PROD_ID, ASIN, TITLE, PRICE, IMAGE_URL, DESCRIPTION) values ('2', '212Qh3dT1mL._AA160_', 'Microfiber Sand Color Swivel Glider Recliner with Ottoman ', 164.98, './img/recliner.jpg', 'This beautiful Glider Rocker recliner from comes with a round base ottoman and made in sand color microfiber. It has a round swivel base with metal and leather armrests with slightly overstuffed cushions. The contrast between the leatherette tone and metal black tubing offers an interesting focal point. The piece is suitable for dens, living rooms, or even a home office. Stylish, convenient and affordable. Sit back, recline, rock, glide and relax. Lowest factory direct price guarantee. ');
insert into PRODUCT_ACTORS (PROD_ID,ACTOR_ID) values (2, 68)
insert into PRODUCT_CATEGORY (PROD_ID, CATEGORY) values (2, 1);
insert into PRODUCT_CATEGORY (PROD_ID, CATEGORY) values (2, 2);
insert into PRODUCT_CATEGORY (PROD_ID, CATEGORY) values (2, 3);
insert into PRODUCT_CATEGORY (PROD_ID, CATEGORY) values (2, 4);
insert into INVENTORY (INV_ID, PROD_ID, QUAN_IN_STOCK, SALES) values (2, 2, 77,32);


insert into PRODUCTS (PROD_ID, ASIN, TITLE, PRICE, IMAGE_URL, DESCRIPTION) values ('3', '21Upbbe5v6L._AA160_', 'Mission Natural Coffee Table', 69.99, './img/coffee-table.jpg', 'Featuring the unadorned beauty and simplicity of mission style, this coffee table from Furio makes a stunning centerpiece—yet without overshadowing the décor. It''s made of rubberwood and has a light natural finish. As perfect for decorative accents as it is for tired feet, it has a sturdy, all-wood construction and minimalist, functional design. A lower shelf offers a place to store books and magazines. 16-1/2Hx40-1/2Wx20D" ');
insert into PRODUCT_ACTORS (PROD_ID,ACTOR_ID) values (3, 68)
insert into PRODUCT_CATEGORY (PROD_ID, CATEGORY) values (3, 2);
insert into PRODUCT_CATEGORY (PROD_ID, CATEGORY) values (3, 3);
insert into PRODUCT_CATEGORY (PROD_ID, CATEGORY) values (3, 4);
insert into INVENTORY (INV_ID, PROD_ID, QUAN_IN_STOCK, SALES) values (3, 3, 79, 31);

insert into PRODUCTS (PROD_ID, ASIN, TITLE, PRICE, IMAGE_URL, DESCRIPTION) values ('4', '21EZ34KY9EL._AA160_', 'RTA Furniture 5649AQ Snack Table in Oak Finished', 34.99, './img/snack-table.jpg', 'Snack Table in Solid Oak Finished, Dimension 13 3/4 inches x 12 inches x 24 inches H, Weight 25 lbs., Pack: 1 box/2.5'' ');

insert into PRODUCT_ACTORS (PROD_ID,ACTOR_ID) values (4, 68)
insert into PRODUCT_CATEGORY (PROD_ID, CATEGORY) values (4, 1);
insert into PRODUCT_CATEGORY (PROD_ID, CATEGORY) values (4, 2);
insert into PRODUCT_CATEGORY (PROD_ID, CATEGORY) values (4, 5);
insert into INVENTORY (INV_ID, PROD_ID, QUAN_IN_STOCK, SALES) values (4, 4, 28, 30);


insert into PRODUCTS (PROD_ID, ASIN, TITLE, PRICE, IMAGE_URL, DESCRIPTION) values ('5', '11PgsxMDwRL._AA160_', 'Platform Bed with Storage ', 289.98, './img/bed.jpg', 'What We Like About the Platform BedWith three drawers on the twin and six drawers on the full and queen sizes this platform bed is an attractive and practical way to store your clothing or bed linens. The location of the spacious drawers completely eliminates the need for a box spring and this platform bed comes ready to use.  The drawers are each 19 inches deep to provide plenty of storage space.  They run smoothly on metal drawer glides and have finger pulls at the bottom of the drawers.  This eliminates the need for knobs on the drawers and ensures nothing gets caught on the drawers. Some other added features of these beds include solid wood slats metal rails for added stability as well as the hardware at the headboard/footboard and side rails which allows for easy assembly and reassembly. This bed is available in several sizes and finishes.To complete your bedroom set add items from the Monterrey collection if you''re selecting either a white or cherry finish or the Sonoma collection if you''re selecting either black or maple finishes. Some collection pieces are available below.  More are available after you''ve added your bed to the shopping cart. Ready-to-assemble and made of durable composite woods this sturdy platform bed is sure to provide you with years of satisfaction.Bed Dimensions:Twin: 76.5L x 41W x 19H inchesFull: 76.5L x 57W x 19H inchesQueen: 82L x 63W x 19H inches');

insert into PRODUCT_ACTORS (PROD_ID,ACTOR_ID) values (5, 68)
insert into PRODUCT_CATEGORY (PROD_ID, CATEGORY) values (5, 2);
insert into INVENTORY (INV_ID, PROD_ID, QUAN_IN_STOCK, SALES) values (5, 5, 28, 29);


insert into PRODUCTS (PROD_ID, ASIN, TITLE, PRICE, IMAGE_URL, DESCRIPTION) values ('6', '21GvIl4-yhL._AA160_', 'Chelsea Bedroom Collection Furniture Collections World Imports Furniture World Imports Bedrooms', 419.98, './img/bedroom.jpg', 'Sleek and Clean Contemporary Design - Solid Wood and Wood Veneers in a Dark Expresso Color - Stylish Bycast PU Headboard Panels - Raised Panels in Wood Footboard - Contemporary Round Satin Silver Drawer Pulls - 2-Piece TV Armoire Available - All Wood Drawer Construction Note :  This Price is for Queen Bed with Wooden Rails only');
insert into PRODUCT_ACTORS (PROD_ID,ACTOR_ID) values (6, 68)
insert into PRODUCT_CATEGORY (PROD_ID, CATEGORY) values (6, 2);
insert into INVENTORY (INV_ID, PROD_ID, QUAN_IN_STOCK, SALES) values (6, 6, 73, 28);


insert into PRODUCTS (PROD_ID, ASIN, TITLE, PRICE, IMAGE_URL, DESCRIPTION) values ('7', '21zCwmFUN1L._AA160_', 'Studio Loft Bed 4037 ', 425.00, './img/studio-loft-bed.jpg', 'Studio Loft Bed Everything you Need Under One Bed! In Satin Silver Finish.Full Size Bed W/Slats,3 In.SQuare Steel post Sleek & Sturdy. Dual-Postion Easy-StepLadder w/Flat Rungs.Large Desk W/L-Turn,Keyboard Drawer,2BookShelves,2CD Racks.Futon Chair 4029(ML)(Optional)Convertible To A bed for Sleep-Over Friend. Top bed uses a regular Full Size mattress (not included in the price). Contact us for shipping cost.');
insert into PRODUCT_ACTORS (PROD_ID,ACTOR_ID) values (7, 68)
insert into PRODUCT_CATEGORY (PROD_ID, CATEGORY) values (7, 2);
insert into PRODUCT_CATEGORY (PROD_ID, CATEGORY) values (7, 4);
insert into PRODUCT_CATEGORY (PROD_ID, CATEGORY) values (7, 5);
insert into PRODUCT_CATEGORY (PROD_ID, CATEGORY) values (7, 6);
insert into INVENTORY (INV_ID, PROD_ID, QUAN_IN_STOCK, SALES) values (7, 7, 95, 28);
insert into PRODUCTS (PROD_ID, ASIN, TITLE, PRICE, IMAGE_URL, DESCRIPTION) values ('8', '11Yw-aDb-EL._AA160_', 'South Shore 3159PPKG Back Bay Platform Bedroom Set - Beds', 250.00, './img/bed-2.jpg', 'Corners rounded for safety Constructed of durable particleboard with a laminate finish Dimensions:  10 H x 54-60 . Platform Bed, $150 to $300, Dark Wood, Beds, Wood, South Shore, Assembly Required,');
insert into PRODUCT_ACTORS (PROD_ID,ACTOR_ID) values (8, 68)
insert into PRODUCT_CATEGORY (PROD_ID, CATEGORY) values (8, 2);
insert into INVENTORY (INV_ID, PROD_ID, QUAN_IN_STOCK, SALES) values (8, 8, 65, 1);

insert into PRODUCTS (PROD_ID, ASIN, TITLE, PRICE, IMAGE_URL, DESCRIPTION) values ('9', '21WQCZGMEJL._AA160_', 'Solid Wood Platform Bed - Twin Size unfinished', 109.98, './img/twin.jpg', 'Economical solid-wood platform bed. Simple, Solid yet Stylish - A Perenial Favorite. The 15" high platform bed height is perfect for a single mattress or futon. In addition, the 10 1/2" underbed clearance proivdes ample storage space below. Made of premium, solid southern yellow pine. While OK to leave unfinished, you can easily add a hand-rubbed Danish/Tung Oil finish or stain to match any decor. Comes ready-to-assemble in 2 compact boxes for easy transportation (also make finishing very simple). No tools required, allen wrenches included. NOTE: Full size bed shown & underbed drawers sold separately');
insert into PRODUCT_ACTORS (PROD_ID,ACTOR_ID) values (9, 68)
insert into PRODUCT_CATEGORY (PROD_ID, CATEGORY) values (9, 2);
insert into INVENTORY (INV_ID, PROD_ID, QUAN_IN_STOCK, SALES) values (9, 9, 24, 11);


insert into PRODUCTS (PROD_ID, ASIN, TITLE, PRICE, IMAGE_URL, DESCRIPTION) values ('10', '11RyfyWB6mL._AA160_', 'White Leather Sectional 662 Clearance & Sales !!! Chair, Sofa and Sleep Sofa', 1874.99, './img/sofa.jpg', 'White Leather sectional with Chrome legs. Perfect for a contemporary living room or den. Dimensions:  91" x 145" x 42" x 32" H ');
insert into PRODUCT_ACTORS (PROD_ID,ACTOR_ID) values (10, 68)
insert into PRODUCT_CATEGORY (PROD_ID, CATEGORY) values (10, 1);
insert into PRODUCT_CATEGORY (PROD_ID, CATEGORY) values (10, 2);
insert into PRODUCT_CATEGORY (PROD_ID, CATEGORY) values (10, 3);
insert into PRODUCT_CATEGORY (PROD_ID, CATEGORY) values (10, 4);
insert into INVENTORY (INV_ID, PROD_ID, QUAN_IN_STOCK, SALES) values (10, 10, 26, 6);


insert into PRODUCTS (PROD_ID, ASIN, TITLE, PRICE, IMAGE_URL, DESCRIPTION) values ('11', '21Ulo1NNKyL._AA160_', 'Laguna Beach Sectional II 2 Pc. Sectional', 119.98, './img/sofa-laguna.jpg', 'Sea Spray. Our Laguna Beach II convertible sectional collection exudes a special coastal ambiance, and will no doubt make your home feel like your very own resort. Cool plush faux suede, clean lines, track arms and chrome feet create a breezy, casual feel. And the handy floating chaise allows you to set up the sectional to feature a chaise on the left or right - you decide! You''ll find this sectional so relaxing and carefree, you may never want to leave home again. Two piece sectional includes floating chaise and sofa, as shown. Also available in lagoon and stone.');
insert into PRODUCT_ACTORS (PROD_ID,ACTOR_ID) values (11, 1)
insert into PRODUCT_CATEGORY (PROD_ID, CATEGORY) values (11, 1);
insert into PRODUCT_CATEGORY (PROD_ID, CATEGORY) values (11, 2);
insert into PRODUCT_CATEGORY (PROD_ID, CATEGORY) values (11, 3);
insert into PRODUCT_CATEGORY (PROD_ID, CATEGORY) values (11, 4);
insert into INVENTORY (INV_ID, PROD_ID, QUAN_IN_STOCK, SALES) values (11, 11, 49, 8);


insert into PRODUCTS (PROD_ID, ASIN, TITLE, PRICE, IMAGE_URL, DESCRIPTION) values ('12', '11cQNX9qArL._AA160_', 'Queen Paris Platform Bed in Sand Color Clearance & Sales !!! Bedroom Furniture Section', 599.99, './img/bed-empire.jpg', 'Price is for the paris bed only in Sand color (not as pictured). Night tables and dressers are not included.');

insert into PRODUCT_ACTORS (PROD_ID,ACTOR_ID) values (12, 1)
insert into PRODUCT_CATEGORY (PROD_ID, CATEGORY) values (12, 1);
insert into PRODUCT_CATEGORY (PROD_ID, CATEGORY) values (12, 2);
insert into PRODUCT_CATEGORY (PROD_ID, CATEGORY) values (12, 3);
insert into INVENTORY (INV_ID, PROD_ID, QUAN_IN_STOCK, SALES) values (12, 12, 40, 8);

insert into PRODUCTS (PROD_ID, ASIN, TITLE, PRICE, IMAGE_URL, DESCRIPTION) values ('13', '21KOXaXEuzL._AA160_', 'Laguna Beach Sectional 2 Pc. Sectional ', 29.99, './img/sofa-beach.jpg', 'Sea Spray. Our Laguna Beach convertible sectional collection exudes a special coastal ambiance, and will no doubt make your home feel like your very own resort. Cool plush faux suede, clean lines, track arms and chrome feet create a breezy, casual feel. You''ll find this collection so relaxing and carefree, you may never want to leave home again. And the handy floating chaise allows you to set up the sectional to feature a chaise on the left or right - you decide! Two piece sectional includes floating chaise and sofa, as shown. Also available in moss and stone.');
insert into PRODUCT_ACTORS (PROD_ID,ACTOR_ID) values (13, 1)
insert into PRODUCT_CATEGORY (PROD_ID, CATEGORY) values (13, 1);
insert into PRODUCT_CATEGORY (PROD_ID, CATEGORY) values (13, 2);
insert into PRODUCT_CATEGORY (PROD_ID, CATEGORY) values (13, 3);
insert into PRODUCT_CATEGORY (PROD_ID, CATEGORY) values (13, 4);
insert into INVENTORY (INV_ID, PROD_ID, QUAN_IN_STOCK, SALES) values (13, 13, 87, 2);
