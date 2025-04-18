#!/usr/bin/env node

//REQUIRED FILES
var Git = require('nodegit');
var co = require('co');
var prompt = require('co-prompt');
var program = require('commander');
var shell = require('shelljs');
var path = require('path');
var chalk = require('chalk');
var exec = require('child_process').exec;
var fs = require('fs');
var ProgressBar = require('progress');

program
		.action(function(){
			co(function *(){

				//PROMPT VARIABLES

				var application = yield prompt(chalk.bold.cyan('application: '))
				var user = yield prompt(chalk.bold.cyan('user: '));
				var branch = yield prompt(chalk.bold.cyan('branch: '));

				//SUPPORTS - WEB | ANDROID | IOS 
				var appType = ''

				//SET VARIABLE TO WEB/iOS/Android DEPENDING ON APPLICATION VALUE
				if(application == 'lattis_ios'){
					appType = 'lattis_ios';
				}else if (application == 'skylock_ios'){
					appType = 'skylock_ios';
				}else if(application == 'lattis-dashboard'){
					appType = 'web';
				}else if(application == 'Skyfleet-Android'){
					appType = 'android';
				}else{
					appType = 'not supported';
				}

				//STATEMENT
				console.log(chalk.yellow('Building file...'));

				var cloneOptions = {};

				//SET CLONE OPTION OAUTH WITH GITHUBTOKEN
				cloneOptions.fetchOpts = {
					callbacks:{
						certificateCheck: function(){
							return 1;
						},
						credentials: function(){
							return Git.Cred.userpassPlaintextNew(process.env.GITHUBTOKEN, 'x-oauth-basic');
						}
					}
				}

				//SET BRANCH ON REPO
				cloneOptions.checkoutBranch = branch;

				var cloneURL = 'https://github.com/' +  user + '/' + application + '.git';
				var localPath = application + '_' + branch;

				//CLONE REPO METHOD WITH OPTIONS SET
				var cloneRepository = Git.Clone(cloneURL, localPath, cloneOptions);

				//ERROR
				var errorAndAttemptOpen = function(){
					console.log(chalk.red('Something went wrong. Please check your inputs and try again...'));
					process.exit();
				}

				//RUN
				cloneRepository.catch(errorAndAttemptOpen)
					.then(function(repository){
						//STATEMENT
						console.log(chalk.green('Finished cloning ' + localPath));

						//TO DO: DIFFERENTIATE BETWEEN WEB/IOS/ANDROID BUILDS
						switch(appType){
							case 'web':
								//STATEMENT
								console.log(chalk.yellow('building node packages'));

								//BUILD NODE PACKAGES FIRST
								exec('npm install',{cwd: String(process.cwd() + '/' + localPath)}, function(error,stdout,stderr){
									//STATEMENT
									console.log(chalk.green('Finished npm install'));
									//STATEMENT
									console.log(chalk.yellow('Building bower packages'));
									//BUILD BOWER PACKAGES SECOND
									exec('bower install',{cwd: String(process.cwd() + '/' + localPath)}, function(error,stdout,stderr){
										console.log(chalk.green('Finished bower install'));
										//STATEMENT
										console.log(chalk.yellow('booting server...check localhost:19754'));
										//BOOT SERVER
										exec('node bin/skyfleet.js',{cwd: String(process.cwd() + '/' + localPath)}, function(error,stdout,stderr){
											/* SERVER RUNNING */
										});
									});
								});
								break;
							case 'lattis_ios':
								//STATEMENT
								console.log(chalk.yellow('Installing cocoapods'));
								//INSTALL COCOAPODS
								exec('gem install cocoapods',{cwd: String(process.cwd() + '/' + localPath)}, function(error,stdout,stderr){
									//STATEMENT
									console.log(chalk.green('Finished installing cocoapods'));
									console.log(chalk.yellow('Installing pods...'));
									//INSTALL PODS
									exec('pod install',{cwd: String(process.cwd() + '/' + localPath)}, function(error,stdout,stderr){
										console.log(chalk.green('Finished installing pods'));
										//LAUNCH
										exec('open Lattis.xcworkspace',{cwd: String(process.cwd() + '/' + localPath)}, function(error,stdout,stderr){
											console.log(chalk.green('Launched!'));
											process.exit();
										});
									});
								});
								break;
							case 'skylock_ios':
								console.log(chalk.yellow("Running source file..."));
								//INSTALL SOURCE - COCOAPODS AND PODS
								exec('source setup.scr',{cwd: String(process.cwd() + '/' + localPath)}, function(error,stdout,stderr){
									console.log(chalk.green('setup.scr completed'));
									console.log(chalk.yellow('updating submodules....'));
									//UPDATE NECESSARY SUDMODULES
									exec('git submodule update',{cwd: String(process.cwd() + '/' + localPath )}, function(error,stdout,stderr){
										console.log(chalk.green('submodules updated'));
										console.log(chalk.yellow('Launching App.....'))
										//LAUNCH
										exec('open Ellipse.xcworkspace',{cwd: String(process.cwd() + '/' + localPath + '/' + 'Skylock')}, function(error,stdout,stderr){
											console.log(chalk.green('App Launched!'));
											process.exit();
										});
									});
								});
								break;
							case 'android':
								//STATEMENT
								console.log(chalk.yellow('Building android dependencies'));
								//INSTALL DEPENDENCIES
								exec('gradlew installDebug',{cwd: String(process.cwd() + '/' + localPath)}, function(error,stdout,stderr){
									console.log(chalk.green('Finished building dependencies'));
									console.log(chalk.yellow('Launching app....'));
									//LAUNCH (?)
									exec('adb shell am start -a android.intent.action.VIEW -n io.lattis.ellipse/.SplashActivity',{cwd: String(process.cwd() + '/' + localPath)}, function(error,stdout,stderr){
										console.log(chalk.green('Launched!'));
									});
								});
								break;
							default:
								console.log(chalk.red('Error: Please enter valid application'));
								process.exit();
						}

						//process.exit();
					}, function(error){
						/* ... */
					});

			});
		})
		.parse(process.argv);







	





