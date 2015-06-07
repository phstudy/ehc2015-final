gradle dist
rm -rf Team34
unzip Team34.zip
rsync -e ssh -av --exclude=upload.sh ./Team34/ em2:./Team34
