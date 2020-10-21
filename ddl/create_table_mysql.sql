CREATE TABLE OBJECTSTORE
(
   okey varchar(50) NOT NULL,
   ovalue blob NOT NULL,
   opartition varchar(50) NOT NULL,
   PRIMARY KEY (okey, opartition)
)
;