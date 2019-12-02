from coverage import Coverage
from dependencies import DependencyMetric
from quality import Quality

coverage_metric = Coverage()
dependency_metric = DependencyMetric()

print('Code coverage metrics:\n')
coverage_metric.calculate_code_coverage()
print('\n==============================\n')
print('Dependency metrics:\n')
dependency_metric.calculate_dependencies_metrics()
print('\n==============================\n')
print('Quality metrics:\n')
quality = Quality(coverage_metric.cyclomatic_complexity,
                  coverage_metric.total_lines_code)
quality.calculate_quality()
