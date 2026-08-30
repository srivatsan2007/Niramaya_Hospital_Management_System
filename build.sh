#!/bin/bash
set -e
echo "Compiling Niramaya Hospital Management System Java sources..."
mkdir -p out Reports public/Reports
find src -name "*.java" > sources_linux.txt
javac -cp "lib/*" -d out @sources_linux.txt
echo "Build complete!"
