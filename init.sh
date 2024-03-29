export SRV_USERNAME=$(cat /secrets/serviceuser/syfomoteadmin/username)
export SRV_PASSWORD=$(cat /secrets/serviceuser/syfomoteadmin/password)

export LDAP_USERNAME=$(cat /secrets/ldap/ldap/username)
export LDAP_PASSWORD=$(cat /secrets/ldap/ldap/password)

export SPRING_DATASOURCE_URL=$(cat /secrets/moteadmindb/config/jdbc_url)
export SPRING_DATASOURCE_USERNAME=$(cat /secrets/moteadmindb/credentials/username)
export SPRING_DATASOURCE_PASSWORD=$(cat /secrets/moteadmindb/credentials/password)
