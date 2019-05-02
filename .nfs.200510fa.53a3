#!/bin/sh

git checkout master
rm -rf target/public
clojure -A:fig:min
cp -a resources/public/ page
mkdir page/cljs-out
cp -a target/public/cljs-out/dev-main.js page/cljs-out/dev-main.js

git checkout gh-pages
cp -a page/ .
git add -A
git commit -m "deploy"
git push
git checkout master
