var gulp = require('gulp');
var less = require('gulp-less');
var watch = require('gulp-watch');

//COMPILE LESS
gulp.task('less',function(){
	gulp.src('./less/styles.less')
	.pipe(less())
	.pipe(gulp.dest('./public/stylesheets'))
});

//WATCH LESS FILES
gulp.task('watch', function(){
	gulp.watch('./less/styles.less', ['less'])
});

//RUN ON 'GULP'
gulp.task('default', ['less','watch']);

