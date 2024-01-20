import numpy as np
import pandas as pd
from travispy import TravisPy, Commit

projects = [
	'rails/rails',
	'elastic/elasticsearch', 
	'jekyll/jekyll', 
	'rg3/youtube-dl',
	'scrapy/scrapy', 
	'mitchellh/vagrant', 
	'scikit-learn/scikit-learn', 
	'ipython/ipython', 
	'capistrano/capistrano', 
	'thoughtbot/paperclip', 
	'spree/spree',
	'resque/resque',
	'fabric/fabric',
	'jnicklas/capybara',
	'netty/netty',
	'elastic/logstash',
	'boto/boto',
	'middleman/middleman',
	'cucumber/cucumber-ruby',
	'celery/celery',
	'thoughtbot/factory_girl',
	'dropwizard/dropwizard',
	'chef/chef',
	'divio/django-cms',
	'fluent/fluentd',
	'norman/friendly_id',
	'flyerhzm/bullet',
	'dropwizard/metrics',
	'airblade/paper_trail',
	'alexreisner/geocoder',
	'slim-template/slim',
	'sparklemotion/nokogiri',
	'binarylogic/authlogic',
	'fog/fog',
	'bottlepy/bottle',
	'sferik/twitter',
	'gradle/gradle',
	'bundler/bundler',
	'activemerchant/active_merchant',
	'refinery/refinerycms',
	'opal/opal',
	'railsbp/rails_best_practices',
	'benoitc/gunicorn',
	'vcr/vcr',
	'haml/haml',
	'mopidy/mopidy',
	'sympy/sympy',
	'rack/rack',
	'django-extensions/django-extensions',
	'jeremyevans/sequel',
	'paramiko/paramiko',
	'rubinius/rubinius',
	'apotonick/cells',
	'jruby/jruby',
	'mikel/mail',
	'aasm/aasm',
	'scipy/scipy',
	'thoughtbot/clearance',
	'octokit/octokit.rb',
	'buildbot/buildbot',
	'grosser/parallel',
	'wvanbergen/request-log-analyzer',
	'troessner/reek',
	'ruboto/ruboto',
	'markevans/dragonfly',
	'grails/grails-core',
	'grosser/parallel_tests',
	'mongodb/mongo-python-driver',
	'scambra/devise_invitable',
	'dennisreimann/ioctocat',
	'cython/cython',
	'mongomapper/mongomapper',
	'publify/publify'	
]

t = TravisPy()

builds_result = []

for project in projects:

	builds = t.builds(slug=project, event_type='pull_request')

	labels = ['project', 'pull_number', 'started_at', 'finished_at', 'branch', 'build_status']
	count = 1

	print ' {:_<6} {:_<26} {:_^12} {:_^8} {:_^8} '.format('','','','','')
	print '|{:^6}|{:^26}|{:^12}|{:^8}|{:^8}|'.format('#','project','pull_number', 'branch', 'status')

	while builds:
		for build in builds:
			print '|{:_<6}|{:_<26}|{:_^12}|{:_^8}|{:_^8}|'.format('','','','','')
			print '|{:^6}|{:^26}|{:^12}|{:^8}|{:^8}|'.format(count, project, build.pull_request_number, build.commit.branch, build.color)

			data = [project, build.pull_request_number, 
					build.started_at, build.finished_at, build.commit.branch, build.color]

			builds_result.append(data)
			count+= 1

		print '|{:_<6}|{:_<26}|{:_^12}|{:_^8}|{:_^8}|'.format('','','','','')

		if not(builds): 
			break		

		last_build_number = builds[-1].number

		builds = t.builds(slug=project, event_type='pull_request', after_number=last_build_number)

	project_df = pd.DataFrame(data=builds_result, columns=labels)
	project_df.to_csv('builds_result_rails.csv')