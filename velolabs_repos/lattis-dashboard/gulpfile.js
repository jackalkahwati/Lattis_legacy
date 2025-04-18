'use strict'

const gulp = require('gulp')
const less = require('gulp-less')
const cleanCSS = require('gulp-clean-css')
const purifyCSS = require('gulp-purifycss')
const rename = require('gulp-rename')

// Less compilation
function lessTask () {
  return gulp.src('./public/stylesheets/less/main.less')
    .pipe(less())
    .pipe(gulp.dest('./public/stylesheets/less/'))
}

// Purify CSS (removes unused selectors)
function purifyTask () {
  return gulp.src('./public/stylesheets/less/main.css')
    .pipe(purifyCSS([
      './public/js/**/*.js',
      './public/libs/cal-heatmap/**/*.js',
      './public/html/**/*.html'
    ]))
    .pipe(rename('main.purify.css'))
    .pipe(gulp.dest('./public/stylesheets/less/'))
}

// Minify the purified CSS
function minifyTask () {
  return gulp.src('./public/stylesheets/less/main.purify.css')
    .pipe(cleanCSS())
    .pipe(rename('main.min.css'))
    .pipe(gulp.dest('./public/stylesheets/less/'))
}

// Watch for changes in LESS files
function watchTask () {
  gulp.watch(
    './public/stylesheets/less/**/*.less',
    gulp.series(lessTask, purifyTask, minifyTask)
  )
}

// Public task names to retain backward‑compatibility with previous gulp <4 CLI
gulp.task('less', lessTask)
gulp.task('purify', gulp.series(lessTask, purifyTask))
gulp.task('minify', gulp.series(lessTask, purifyTask, minifyTask))
gulp.task('watch', watchTask)

// Default task — build then watch
gulp.task('default', gulp.series(gulp.parallel('less', 'purify', 'minify'), 'watch'))
