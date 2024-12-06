#!/bin/sh

# Welcome script for Corese-Command

# If no arguments are provided, display the quick start guide
if [ "$#" -eq 0 ]; then
    # Display the welcome message
    echo -e "\033[44;1;37mWelcome to Corese-Command!\033[0m"
    echo -e "\n\033[1mQuick Setup Guide:\033[0m\n"

    # Step-by-step instructions
    echo -e "\033[1;34mStep 1:\033[0m Open a terminal window.\n"

    echo -e "\033[1;34mStep 2:\033[0m Add the alias to your shell's configuration file for permanent use:\n"
    
    # Instructions for Bash
    echo -e "  \033[1;36m- For Bash:\033[0m"
    echo -e "  \033[1;33mecho \"alias corese='flatpak run fr.inria.corese.CoreseCommand'\" >> ~/.bashrc\033[0m"
    echo -e "  \033[1;32mTip:\033[0m Refresh your configuration with: \033[1;33msource ~/.bashrc\033[0m\n"

    # Instructions for Zsh
    echo -e "  \033[1;36m- For Zsh:\033[0m"
    echo -e "  \033[1;33mecho \"alias corese='flatpak run fr.inria.corese.CoreseCommand'\" >> ~/.zshrc\033[0m"
    echo -e "  \033[1;32mTip:\033[0m Refresh your configuration with: \033[1;33msource ~/.zshrc\033[0m\n"

    # Instructions for Fish
    echo -e "  \033[1;36m- For Fish:\033[0m"
    echo -e "  \033[1;33malias corese='flatpak run fr.inria.corese.CoreseCommand'; funcsave corese\033[0m\n"

    echo -e "\033[1;34mStep 3:\033[0m To use Corese-Command, simply type 'corese' followed by any options. For example:"
    echo -e "\033[1;33mcorese -h\033[0m\n"

    echo -e "Press any key to close this terminal window."
  
    # Wait for user action before closing
    read -n 1 -s
else
    # Execute the JAR file with the provided arguments
    exec /app/jre/bin/java -jar /app/bin/corese-command-standalone.jar "$@"
fi
