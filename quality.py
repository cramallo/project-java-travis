import os
import xml.etree.ElementTree as ET


class Quality:
    def __init__(self, cyclomatic_complexity, lines_of_code):
        self.cyclomatic_complexity = cyclomatic_complexity
        self.lines_of_code = lines_of_code

    def set_variables(self):
        self.pmd_report = './build/reports/pmd/main.xml'
        self.cpd_report = './build/reports/cpd/cpdCheck.xml'

    def calculate_duplicate_code(self):
        self.set_variables()
        tree = ET.parse(self.cpd_report)
        root = tree.getroot()
        non_duplication_score = 100

        for elem in root:
            non_duplication_score -= 0.5

        score = self.calculate_score_duplications(non_duplication_score)
        print('Duplicated code metric:\n')
        print('Non duplicate code score: ' + str(non_duplication_score))
        print('------------------------------')
        print('Score: ' + score)
        print('------------------------------')

    def calculate_code_smells(self):
        self.set_variables()
        tree = ET.parse(self.pmd_report)
        root = tree.getroot()
        total_issues = 0
        high_priority = 0
        medium_priority = 0
        low_priority = 0

        for referece_file in root:
            for violation in referece_file:
                priority = int(violation.attrib['priority'])
                if(priority == 1 or priority == 2):
                    high_priority += 1
                elif(priority == 3):
                    medium_priority += 1
                elif(priority == 4 or priority == 5):
                    low_priority += 1
                total_issues += 1

        high_priority_percentage = high_priority * 100 / self.lines_of_code
        medium_priority_percentage = medium_priority * 100 / self.lines_of_code
        low_priority_percentage = low_priority * 100 / self.lines_of_code

        high_priority_weighing = high_priority_percentage * 0.60
        medium_priority_weighing = medium_priority_percentage * 0.30
        low_priority_weighing = low_priority_percentage * 0.10

        code_smells_ratio = (high_priority_weighing +
                             medium_priority_weighing + low_priority_weighing) / 3

        score = str(self.calculate_score_code_smells(code_smells_ratio))

        print('Code smells:\n')
        print('Total issues: '+str(total_issues))
        print('High priority: '+str(high_priority))
        print('Medium priority: '+str(medium_priority))
        print('Low priority: '+str(low_priority))
        print('Code smells ratio: ' + str(code_smells_ratio))
        print('Score: ' + score)
        print('------------------------------')

        return code_smells_ratio

    def calculate_score_duplications(self, non_duplication_score):
        score = ''
        if(non_duplication_score <= 20):
            score = 'E'
        elif(non_duplication_score >= 21 and non_duplication_score <= 50):
            score = 'D'
        elif(non_duplication_score >= 51 and non_duplication_score <= 60):
            score = 'C'
        elif(non_duplication_score >= 61 and non_duplication_score <= 70):
            score = 'B'
        else:
            score = 'A'
        return score

    def calculate_score_code_smells(self, code_smells_ratio):
        score = ''
        if(code_smells_ratio <= 5):
            score = 'A'
        elif(code_smells_ratio >= 6 and code_smells_ratio <= 10):
            score = 'B'
        elif(code_smells_ratio >= 11 and code_smells_ratio <= 20):
            score = 'C'
        elif(code_smells_ratio >= 21 and code_smells_ratio <= 50):
            score = 'D'
        else:
            score = 'E'
        return score

    def calculate_quality(self):
        self.set_variables()
        self.calculate_duplicate_code()
        self.calculate_code_smells()
        print('Cyclomatic complexity: ' + str(self.cyclomatic_complexity))
