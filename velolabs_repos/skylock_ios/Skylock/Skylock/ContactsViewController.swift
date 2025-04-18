//
//  ContactsViewController.swift
//  Ellipse
//
//  Created by Ravil Khusainov on 28/01/2017.
//  Copyright © 2017 Andre Green. All rights reserved.
//

import UIKit
import Contacts
import Localize_Swift
import SwiftyTimer

class ContactsViewController: SLBaseViewController {
    internal var sections: [Section] = []
    internal let tableView = UITableView()
    internal let cellIdentifire = String(describing: EllipseContactCell.self)
    internal let sectionIdentifire = String(describing: ContactsSectionView.self)
    internal let searchBar :UISearchBar = {
        let bar = UISearchBar()
        bar.placeholder = "Search".localized()
        bar.barTintColor = .slBluegrey
        bar.returnKeyType = .done
        bar.enablesReturnKeyAutomatically = false
        return bar
    }()
    fileprivate let contactsHandler = SLContactHandler()
    fileprivate var allSections: [Section] = []
    
    override func viewDidLoad() {
        super.viewDidLoad()
        
        UIBarButtonItem.appearance().setTitleTextAttributes([NSForegroundColorAttributeName: UIColor.white], for: .normal)
        
        addBackButton()
        fetchContacts()
        configureTableView()
        

        NotificationCenter.default.addObserver(self, selector: #selector(keyboardWillShow(notification:)), name: .UIKeyboardWillShow, object: nil)
        NotificationCenter.default.addObserver(self, selector: #selector(keyboardWillDisappear(notification:)), name: .UIKeyboardWillHide, object: nil)
    }
    
    func keyboardWillShow(notification: Notification) {
        guard let info = notification.userInfo, let frameValue = info[UIKeyboardFrameEndUserInfoKey] as? NSValue else { return }
        let keyboardFrame: CGRect = frameValue.cgRectValue
        
        var inset = tableView.contentInset
        inset.bottom =  keyboardFrame.height
        
        self.tableView.contentInset = inset
    }
    
    func keyboardWillDisappear(notification: Notification) {
        guard let info = notification.userInfo,
            let durration:Double = info[UIKeyboardAnimationDurationUserInfoKey] as? Double,
            let curve = info[UIKeyboardAnimationCurveUserInfoKey] as? NSNumber else { return }
        
        var inset = tableView.contentInset
        inset.bottom = 0
        UIView.animate(withDuration: durration, delay: 0, options: UIViewAnimationOptions(rawValue: curve.uintValue), animations: {
            self.tableView.contentInset = inset
        }, completion: nil)
    }
    
    private func fetchContacts() {
        DispatchQueue.global().async {
            do {
                try self.contactsHandler.allContacts(completion: { (contacts) in
                    self.process(contacts: contacts.filter({$0.phoneNumber != nil}).sorted(by: { $0.fullName < $1.fullName }))
                })
            } catch {
                print("error retreiving contacts")
            }
        }
    }
    
    private func process(contacts: [CNContact]) {
        allSections = sections(from: contacts)
        sections = allSections
        DispatchQueue.main.async {
            self.tableView.reloadData()
        }
    }
    
    fileprivate func sections(from contacts: [CNContact]) -> [Section] {
        var sections: [Section] = []
        for contact in contacts {
            guard let headerChar = contact.fullName.characters.first else { continue }
            let header = String(headerChar).uppercased()
            var section = sections.last
            
            if section?.header == header {
                section?.contacts += contact.ellipse
                _ = sections.removeLast()
                sections.append(section!)
            } else {
                sections.append(Section(header: header, contacts: contact.ellipse))
            }
        }
        
        return sections
    }
    
    fileprivate func sections(from contacts: [EllipseContact]) -> [Section] {
        var sections: [Section] = []
        for contact in contacts {
            guard let headerChar = contact.name.characters.first else { continue }
            let header = String(headerChar).uppercased()
            var section = sections.last
            
            if section?.header == header {
                section?.contacts.append(contact)
                _ = sections.removeLast()
                sections.append(section!)
            } else {
                sections.append(Section(header: header, contacts: [contact]))
            }
        }
        
        return sections
    }
    
    private func configureTableView() {
        view.addSubview(tableView)
        tableView.translatesAutoresizingMaskIntoConstraints = false
        tableView.constrainEdges(to: view)
        tableView.backgroundColor = .color(242, green: 242, blue: 242)
        tableView.register(EllipseContactCell.self, forCellReuseIdentifier: cellIdentifire)
        tableView.register(ContactsSectionView.self, forHeaderFooterViewReuseIdentifier: sectionIdentifire)
        tableView.separatorInset = UIEdgeInsets(top: 0, left: 7, bottom: 0, right: 0)
        tableView.rowHeight = EllipseContactCell.rowHeight
        tableView.sectionHeaderHeight = 25
        tableView.tableFooterView = UIView()
        tableView.delegate = self
        tableView.dataSource = self
        
        searchBar.frame = {
            var frame = view.bounds
            frame.size.height = 44
            return frame
        }()
        tableView.tableHeaderView = searchBar
        searchBar.delegate = self
    }
    
    internal func didSelect(contact: EllipseContact, at indexPath: IndexPath) {
        print("Ovverrirde \(#function) method")
    }
}


// MARK: - UITableViewDelegate, UITableViewDataSource
extension ContactsViewController: UITableViewDelegate, UITableViewDataSource {
    func numberOfSections(in tableView: UITableView) -> Int {
        return sections.count
    }
    
    func tableView(_ tableView: UITableView, numberOfRowsInSection section: Int) -> Int {
        return sections[section].contacts.count
    }
    
    func sectionIndexTitles(for tableView: UITableView) -> [String]? {
        return "ABCDEFGHIJKLMNOPQRSTUVWXYZ".characters.flatMap{String($0)}
    }
    
    func tableView(_ tableView: UITableView, sectionForSectionIndexTitle title: String, at index: Int) -> Int {
        return sections.map({ $0.header }).filter{$0 <= title}.count - 1
    }
    
    func tableView(_ tableView: UITableView, cellForRowAt indexPath: IndexPath) -> UITableViewCell {
        let cell = tableView.dequeueReusableCell(withIdentifier: cellIdentifire, for: indexPath) as! EllipseContactCell
        cell.contact = sections[indexPath.section].contacts[indexPath.row]
        return cell
    }
    
    func tableView(_ tableView: UITableView, viewForHeaderInSection section: Int) -> UIView? {
        let view = tableView.dequeueReusableHeaderFooterView(withIdentifier: sectionIdentifire) as? ContactsSectionView
        view?.titleLabel.text = sections[section].header
        return view
    }
    
    func tableView(_ tableView: UITableView, didSelectRowAt indexPath: IndexPath) {
        let contact = sections[indexPath.section].contacts[indexPath.row]
        didSelect(contact: contact, at: indexPath)
        Timer.after(0.35.seconds) {
            tableView.deselectRow(at: indexPath, animated: true)
        }
    }
}

// MARK: - UISearchBarDelegate
extension ContactsViewController: UISearchBarDelegate {
    func searchBarTextDidBeginEditing(_ searchBar: UISearchBar) {
        searchBar.setShowsCancelButton(true, animated: true)
    }
    
    func searchBarTextDidEndEditing(_ searchBar: UISearchBar) {
        searchBar.setShowsCancelButton(false, animated: true)
    }
    
    func searchBarCancelButtonClicked(_ searchBar: UISearchBar) {
        searchBar.text = nil
        searchBar.resignFirstResponder()
        sections = allSections
        tableView.reloadData()
    }
    
    func searchBar(_ searchBar: UISearchBar, textDidChange searchText: String) {
        DispatchQueue.global().async {
            if searchText.characters.count > 1 {
                let contacts = self.allSections.flatMap({ $0.contacts })
                self.sections = self.sections(from: contacts.filter({ contact -> Bool in
                    return contact.name.lowercased().contains(searchText.lowercased()) || contact.phoneNumber.contains(searchText)
                }).sorted(by: { $0.name < $1.name }))
            } else {
                self.sections = self.allSections
            }
            DispatchQueue.main.async {
                self.tableView.reloadData()
            }
        }
    }
    
    func searchBarSearchButtonClicked(_ searchBar: UISearchBar) {
        searchBar.resignFirstResponder()
    }
}

internal extension ContactsViewController {
    struct Section {
        let header: String
        var contacts: [EllipseContact]
    }
}

struct EllipseContact {
    let phoneNumber: String
    let firstName: String?
    let lastName: String?
    let countryCode: String?
    let image: UIImage?
    var name: String {
        var name = firstName ?? ""
        if let lastName = lastName {
            if name.isEmpty {
                name = lastName
            } else {
                name += " \(lastName)"
            }
        }
        return name
    }
}

extension EllipseContact {
    init(contact: CNContact, phone: CNPhoneNumber) {
        self.firstName = contact.givenName
        self.lastName = contact.familyName
        self.phoneNumber = phone.stringValue
        self.countryCode = phone.countryCodeString
        if let data = contact.thumbnailImageData {
            self.image = UIImage(data: data)
        } else {
            self.image = nil
        }
    }
}

extension EllipseContact: Equatable {
    public static func ==(lhs: EllipseContact, rhs: EllipseContact) -> Bool {
        return lhs.phoneNumber == rhs.phoneNumber
    }
}

extension CNContact {
    var ellipse: [EllipseContact] {
        var phones: [CNPhoneNumber] = []
        for number in phoneNumbers {
            if phones.contains(where: { $0.stringValue == number.value.stringValue }) {
                continue
            }
            phones.append(number.value)
        }
        return phones.flatMap({ EllipseContact(contact: self, phone: $0) })
    }
}

extension EllipseContact {
    init?(contact: SLEmergencyContact) {
        guard let phoneNumber = contact.phoneNumber else { return nil }
        self.firstName = contact.firstName
        self.lastName = contact.lastName
        self.phoneNumber = phoneNumber
        self.countryCode = contact.countyCode
        self.image = nil
    }
}

extension SLEmergencyContact {
    func fill(contact: EllipseContact) {
        self.firstName = contact.firstName
        self.lastName = contact.lastName
        self.countyCode = contact.countryCode
        self.phoneNumber = contact.phoneNumber
    }
}
