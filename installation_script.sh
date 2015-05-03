# download the jar
wget https://github.com/ubibazaar/ahab/releases/download/v0.1/ubibazaar-ahab-0.1-jar-with-dependencies.jar

# prepare properties
echo "manager_id = {{manager_id}}" >> ahab.properties
echo "manager_key = {{manager_key}}" >> ahab.properties

# copy the jar and properties to a dir
mkdir /usr/local/ubibazaar
cp ahab.properties /usr/local/ubibazaar
cp ubibazaar-ahab-0.1-jar-with-dependencies.jar /usr/local/ubibazaar

# download service and install it
wget https://github.com/ubibazaar/ahab/releases/download/v0.1/ahab
chmod 755 ahab
cp ahab /etc/init.d/

# start the service
/etc/init.d/ahab start
