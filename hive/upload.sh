target_dir=$(basename $(pwd))
echo "sync to ./$target_dir/"
rsync -e ssh -av --exclude=upload.sh ./ em2:./$target_dir/
