//
//  FilterViewController.swift
//  Lattis O&M
//
//  Created by Ravil Khusainov on 07/04/2017.
//  Copyright © 2017 Lattis. All rights reserved.
//

import UIKit
import Localize_Swift

protocol FilterRepresentable {
    var displayName: String { get }
}

enum Filter: FilterRepresentable {
    case all, my
    var displayName: String {
        switch self {
        case .all:
            return "tickets_filter_all".localized()
        case .my:
            return "tickets_filter_my".localized()
        }
    }
}

enum Sort: FilterRepresentable {
    case date, distance
    var displayName: String {
        switch self {
        case .date:
            return "tickets_sort_date".localized()
        case .distance:
            return "tickets_sort_distance".localized()
        }
    }
}

class FilterViewController: ViewController {
    @IBOutlet weak var tableView: UITableView!
    var interactor: TicketsInteractorInput!
    
    fileprivate var selected: [Int] = [0, 0]
    
    fileprivate var sections: [Section] = [
        Section(title: "tickets_filter_section_filter".localized(), items: [Filter.all, Filter.my]),
        Section(title: "tickets_filter_section_sorting".localized(), items: [Sort.date, Sort.distance])
    ]
    
    override func viewDidLoad() {
        super.viewDidLoad()

        tableView.register(FilterCell.self, forCellReuseIdentifier: "cell")
        tableView.sectionHeaderHeight = 55
        tableView.sectionFooterHeight = 44
        tableView.tableFooterView = UIView()
        tableView.dataSource = self
        tableView.delegate = self
    }
}

extension FilterViewController: UITableViewDataSource, UITableViewDelegate {
    func numberOfSections(in tableView: UITableView) -> Int {
        return sections.count
    }
    
    func tableView(_ tableView: UITableView, numberOfRowsInSection section: Int) -> Int {
        return sections[section].items.count
    }
    
    func tableView(_ tableView: UITableView, cellForRowAt indexPath: IndexPath) -> UITableViewCell {
        let model = sections[indexPath.section].items[indexPath.row]
        let cell = tableView.dequeueReusableCell(withIdentifier: "cell", for: indexPath) as! FilterCell
        cell.model = model
        cell.isCoosen = selected[indexPath.section] == indexPath.row
        return cell
    }
    
    func tableView(_ tableView: UITableView, viewForFooterInSection section: Int) -> UIView? {
        let view = UIView()
        view.backgroundColor = .white
        return view
    }
    
    func tableView(_ tableView: UITableView, viewForHeaderInSection section: Int) -> UIView? {
        let view = FilterSectionView()
        view.titleLabel.text = sections[section].title
        return view
    }
    
    func tableView(_ tableView: UITableView, didSelectRowAt indexPath: IndexPath) {
        let model = sections[indexPath.section].items[indexPath.row]
        if interactor.didSelect(value: model) {
            selected[indexPath.section] = indexPath.row
            tableView.reloadData()
        }
    }
}

fileprivate extension FilterViewController {
    struct Section {
        var title: String
        var items: [FilterRepresentable]
    }
}

class FilterCell: UITableViewCell {
    var model: FilterRepresentable? {
        didSet {
            textLabel?.text = model?.displayName
        }
    }
    
    var isCoosen: Bool = false {
        didSet {
            imageView?.image = isCoosen ? #imageLiteral(resourceName: "icon_radion_selected") : #imageLiteral(resourceName: "icon_radio_unselected")
        }
    }
    
    override init(style: UITableViewCell.CellStyle, reuseIdentifier: String?) {
        super.init(style: style, reuseIdentifier: reuseIdentifier)
        
        selectionStyle = .none
        textLabel?.font = UIFont.systemFont(ofSize: 12)
        textLabel?.textColor = .lsGreyish
        imageView?.contentMode = .center
    }
    
    required init?(coder aDecoder: NSCoder) {
        fatalError("init(coder:) has not been implemented")
    }
    
    override func layoutSubviews() {
        super.layoutSubviews()
        imageView?.frame = {
            var frame = contentView.bounds
            frame.size.width = 26
            frame.origin.x = contentView.bounds.width - frame.width - 20
            return frame
        }()
        
        textLabel?.frame = {
            var frame = contentView.bounds
            frame.origin.x = 20
            frame.size.width = contentView.bounds.width - frame.minX - 20 - 26 - 8
            return frame
        }()
    }
}

class FilterSectionView: UIView {
    let titleLabel: UILabel = {
        let label = UILabel()
        label.textColor = .lsWarmGreyThree
        label.font = UIFont.systemFont(ofSize: 13)
        return label
    }()
    
    override init(frame: CGRect) {
        super.init(frame: frame)
        backgroundColor = .white
        addSubview(titleLabel)
    }
    
    required init?(coder aDecoder: NSCoder) {
        fatalError("init(coder:) has not been implemented")
    }
    
    override func layoutSubviews() {
        super.layoutSubviews()
        
        titleLabel.frame = bounds.insetBy(dx: 20, dy: 0)
    }
}
