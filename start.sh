#!/bin/bash

config_file="$(dirname "$0")/start-config.properties"
jar_file="$(dirname "$0")/petpet-*.jar"

if [ ! -e "$jar_file" ]; then
    echo "Cannot find petpet jar file."
    read -n 1 -s -r -p "Press any key to continue..."
    exit 1
fi

if [ -e "$config_file" ]; then
    read -r user_option < "$config_file"
    echo "Start Config: $user_option"
else
    read -p "Enter the option (0: WebServer, 1: GoCQ Plugin)[0]: " user_option
    user_option="${user_option:-0}"
    echo "$user_option" > "$config_file"
fi

if [ "$user_option" = "1" ]; then
    args="-gocq"
else
    args=""
fi

for jar_file in $(ls -1 "$jar_file"); do
    :
done

echo "Running $jar_file"
java -jar -Xms16M "$jar_file" $args

exit 0
