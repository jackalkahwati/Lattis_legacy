//
//  CardExpDatePicker.swift
//  LattisCore
//
//  Created by Roger Molas on 9/1/22.
//  Copyright © 2022 Lattis inc. All rights reserved.
//

import Foundation
import Cartography

class CardExpDatePicker: UIViewController {
    
    fileprivate let cardView = UIView()
    fileprivate let pickerView = MonthDatePickerView()
    fileprivate let titleLabel = UILabel.label(font: .theme(weight: .bold, size: .small))
    fileprivate let subtitleLabel = UILabel.label(text: "select_date_time".localized(), font: .theme(weight: .bold, size: .title))
    fileprivate let closeButton = UIButton(type: .system)
    fileprivate let doneButton = ActionButton()
    fileprivate let completion: (Date) -> ()
    
    init(title: String, date: Date, completion: @escaping (Date) -> ()) {
        self.titleLabel.text = title
        self.completion = completion
        super.init(nibName: nil, bundle: nil)
        modalPresentationStyle = .overCurrentContext
    }
    
    required init?(coder: NSCoder) {
        fatalError("init(coder:) has not been implemented")
    }
    
    override func viewDidLoad() {
        super.viewDidLoad()
        
        if #available(iOS 13.0, *) {
            overrideUserInterfaceStyle = .light
        }
        
        view.backgroundColor = UIColor(white: 0.3, alpha: 0.8)
        
        cardView.backgroundColor = .white
        cardView.layer.maskedCorners = [.layerMinXMinYCorner, .layerMaxXMinYCorner]
        cardView.layer.cornerRadius = .containerCornerRadius
        
        view.addSubview(cardView)
        cardView.addSubview(closeButton)
        cardView.addSubview(titleLabel)
        cardView.addSubview(subtitleLabel)
        cardView.addSubview(pickerView)
        cardView.addSubview(doneButton)
        
        closeButton.setImage(.named("icon_close"), for: .normal)
        closeButton.tintColor = .black
        
        constrain(cardView, closeButton, titleLabel, subtitleLabel, pickerView, doneButton, view) { card, close, title, subtitle, picker, done, view in
            card.bottom == view.bottom
            card.left == view.left
            card.right == view.right
            
            done.bottom == view.safeAreaLayoutGuide.bottom - .margin
            done.left == card.left + .margin
            done.right == card.right - .margin
            
            picker.bottom == done.top
            picker.left == card.left
            picker.right == card.right
            
            subtitle.bottom == picker.top
            subtitle.left == card.left + .margin
            subtitle.right == card.right - .margin
            
            title.left == subtitle.left
            title.right == subtitle.right
            title.bottom == subtitle.top
            
            close.bottom == title.top
            close.right == card.right - .margin
            close.top == card.top + .margin
        }
        
        doneButton.action = .plain(title: "done".localized(), handler: done)
        closeButton.addTarget(self, action: #selector(close), for: .touchUpInside)
    }
    
    fileprivate func done() {
        completion(pickerView.date)
        close()
    }
    
    fileprivate func present() {
        view.layoutIfNeeded()
        UIView.animate(withDuration: 0.3, animations: {
            self.view.layoutIfNeeded()
        }, completion: { _ in
            
        })
    }
    
    @objc
    internal override func close() {
        dismiss(animated: true, completion: nil)
    }
}


@objc class MonthDatePickerView: UIPickerView  {

    enum Component: Int {
        case Month = 0
        case Year = 1
    }

    let LABEL_TAG = 43
    let bigRowCount = 1000
    let numberOfComponentsRequired = 2

    let months = ["01", "02", "03", "04", "05", "06", "07", "08", "09", "10", "11", "12"]
    var years: [String] {
        get {
            var years: [String] = [String]()
            for i in minYear...maxYear {
                years.append("\(i)")
            }
            return years;
        }
    }

    var bigRowMonthsCount: Int {
        get {
            return bigRowCount * months.count
        }
    }

    var bigRowYearsCount: Int {
        get {
            return bigRowCount * years.count
        }
    }

    var monthSelectedTextColor: UIColor?
    var monthTextColor: UIColor?
    var yearSelectedTextColor: UIColor?
    var yearTextColor: UIColor?
    var monthSelectedFont: UIFont?
    var monthFont: UIFont?
    var yearSelectedFont: UIFont?
    var yearFont: UIFont?
    let rowHeight: NSInteger = 44

    /**
     Will be returned in user's current TimeZone settings
    **/
    var date: Date {
        get {
            let month = self.months[selectedRow(inComponent: Component.Month.rawValue) % months.count]
            let year = self.years[selectedRow(inComponent: Component.Year.rawValue) % years.count]
            let formatter = DateFormatter()
            formatter.dateFormat = "MM yyyy"
            return formatter.date(from: "\(month) \(year)")!
        }
    }

    var minYear: Int!
    var maxYear: Int!

    override init(frame: CGRect) {
        super.init(frame: frame)
        loadDefaultParameters()
    }

    required init?(coder aDecoder: NSCoder) {
        super.init(coder: aDecoder)
        loadDefaultParameters()
    }

    override func awakeFromNib() {
        super.awakeFromNib()
        loadDefaultParameters()
    }

    func loadDefaultParameters() {
        minYear = Calendar.current.dateComponents([.year], from: Date()).year
        maxYear = minYear! + 10

        self.delegate = self
        self.dataSource = self

        monthSelectedTextColor = .blue
        monthTextColor = .black

        yearSelectedTextColor = .blue
        yearTextColor = .black

        monthSelectedFont = .boldSystemFont(ofSize: 17)
        monthFont = .boldSystemFont(ofSize: 17)

        yearSelectedFont = .boldSystemFont(ofSize: 17)
        yearFont = .boldSystemFont(ofSize: 17)
    }


    func setup(minYear: NSInteger, andMaxYear maxYear: NSInteger) {
        self.minYear = minYear
        if maxYear > minYear {
            self.maxYear = maxYear
        } else {
            self.maxYear = minYear + 10
        }
    }

    func selectToday() {
        selectRow(todayIndexPath.row, inComponent: Component.Month.rawValue, animated: false)
        selectRow(todayIndexPath.section, inComponent: Component.Year.rawValue, animated: false)
    }

    var todayIndexPath: NSIndexPath {
        get {
            var row = 0.0
            var section = 0.0

            for cellMonth in months {
                if cellMonth == currentMonthName {
                    row = Double(months.firstIndex(of: cellMonth)!)
                    row = row + Double(bigRowMonthsCount / 2)
                    break
                }
            }

            for cellYear in years {
                if cellYear == currentYearName {
                    section = Double(years.firstIndex(of: cellYear)!)
                    section = section + Double(bigRowYearsCount / 2)
                    break
                }
            }

            return NSIndexPath(row: Int(row), section: Int(section))
        }
    }

    var currentMonthName: String {
        get {
            let formatter = DateFormatter()
            let locale = Locale.init(identifier: "en_US")
            formatter.locale = locale
            formatter.dateFormat = "MM"
            return formatter.string(from: Date())
        }
    }

    var currentYearName: String {
        get {
            let formatter = DateFormatter()
            formatter.dateFormat = "yyyy"
            return formatter.string(from: Date())
        }
    }


    func selectedColorForComponent(component: NSInteger) -> UIColor {
        if component == Component.Month.rawValue {
            return monthSelectedTextColor!
        }
        return yearSelectedTextColor!
    }

    func colorForComponent(component: NSInteger) -> UIColor {
        if component == Component.Month.rawValue {
            return monthTextColor!
        }
        return yearTextColor!
    }

    func selectedFontForComponent(component: NSInteger) -> UIFont {
        if component == Component.Month.rawValue {
            return monthSelectedFont!
        }
        return yearSelectedFont!
    }

    func fontForComponent(component: NSInteger) -> UIFont {
        if component == Component.Month.rawValue {
            return monthFont!
        }
        return yearFont!
    }

    func titleForRow(row: Int, forComponent component: Int) -> String? {
        if component == Component.Month.rawValue {
            return self.months[row % self.months.count]
        }
        return self.years[row % self.years.count]
    }

    func labelForComponent(component: NSInteger) -> UILabel {
        let frame = CGRect(x: 0.0, y: 0.0, width: bounds.size.width, height: CGFloat(rowHeight))
        let label = UILabel(frame: frame)
        label.textAlignment = .center
        label.backgroundColor = .clear
        label.isUserInteractionEnabled = false
        label.tag = LABEL_TAG
        return label
    }
}

//MARK: - UIPickerViewDelegate, UIPickerViewDataSource
extension MonthDatePickerView: UIPickerViewDelegate, UIPickerViewDataSource {

    func numberOfComponentsInPickerView(pickerView: UIPickerView) -> Int {
        return numberOfComponentsRequired
    }

    func pickerView(_ pickerView: UIPickerView, numberOfRowsInComponent component: Int) -> Int {
        if (component == Component.Month.rawValue) {
            return bigRowMonthsCount
        } else {
            return bigRowYearsCount
        }
    }

    func pickerView(_ pickerView: UIPickerView, widthForComponent component: Int) -> CGFloat {
        return self.bounds.size.width / CGFloat(numberOfComponentsRequired)
    }

    func pickerView(_ pickerView: UIPickerView, viewForRow row: Int, forComponent component: Int, reusing view: UIView?) -> UIView {
        var selected = false
        if component == Component.Month.rawValue {
            let monthName = self.months[(row % self.months.count)]
            if monthName == currentMonthName {
                selected = true
            }
        } else {
            let yearName = self.years[(row % self.years.count)]
            if yearName == currentYearName {
                selected = true
            }
        }

        var returnView: UILabel
        if view?.tag == LABEL_TAG {
            returnView = view as! UILabel
        } else {
            returnView = labelForComponent(component: component)
        }
        returnView.font = selected ? selectedFontForComponent(component: component) : fontForComponent(component: component)
        returnView.textColor = selected ? selectedColorForComponent(component: component) : colorForComponent(component: component)
        returnView.text = titleForRow(row: row, forComponent: component)
        return returnView
    }

    func pickerView(_ pickerView: UIPickerView, rowHeightForComponent component: Int) -> CGFloat { CGFloat(rowHeight) }

    func numberOfComponents(in pickerView: UIPickerView) -> Int { 2 }
}
