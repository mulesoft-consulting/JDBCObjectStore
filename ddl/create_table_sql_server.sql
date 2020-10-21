CREATE TABLE OBJECTSTORE
(
   okey varchar(50) NOT NULL,
   ovalue image NOT NULL,
   opartition varchar(50) NOT NULL,
   PRIMARY KEY (okey, opartition)
)
;