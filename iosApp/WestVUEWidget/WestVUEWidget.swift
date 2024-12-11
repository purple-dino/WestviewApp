//
//  WestVUEWidget.swift
//  WestVUEWidget
//
//  Created by E 9 on 10/9/24.
//  Copyright © 2024 orgName. All rights reserved.
//

import WidgetKit
import SwiftUI
import ComposeApp

struct Provider: TimelineProvider {
    func placeholder(in context: Context) -> SimpleEntry {
        SimpleEntry(date: Date(), currentClass: "Integrated Math 1", timeRemaining: "10 minutes")
    }

    func getSnapshot(in context: Context, completion: @escaping (SimpleEntry) -> ()) {
        let entry = SimpleEntry(date: Date(), currentClass: "Integrated Math 1", timeRemaining: "10 minutes")
        completion(entry)
    }

    func getTimeline(in context: Context, completion: @escaping (Timeline<Entry>) -> ()) {
        var entries: [SimpleEntry] = []
//        let schedule = "\(StudentSharedViewModel().classes?.classes ?? ["hi"])"

        // Generate a timeline consisting of five entries an hour apart, starting from the current date.
        let currentDate = Date()
        for secondOffset in 0 ..< 60 {
            let schedule = "\(StudentSharedViewModel())"
            let entryDate = Calendar.current.date(byAdding: .second, value: secondOffset, to: currentDate)!
            let entry = SimpleEntry(date: entryDate, currentClass: "Integrated Math 1", timeRemaining: schedule)
            entries.append(entry)
        }

        let timeline = Timeline(entries: entries, policy: .atEnd)
        completion(timeline)
    }
}

struct SimpleEntry: TimelineEntry {
    let date: Date
    let currentClass: String
    let timeRemaining: String
}

struct TimeRemainingEntry: TimelineEntry {
    let date: Date
    let timeRemaining: String
}

struct WestVUEWidgetEntryView : View {
    var entry: Provider.Entry

    var body: some View {
        VStack (alignment: .center){
//            Text(entry.timeRemaining)
//            Text(entry.currentClass).multilineTextAlignment(.center)
            WidgetView()
        }
    }
}

struct WidgetView: UIViewControllerRepresentable {
    func makeUIViewController(context: Context) -> UIViewController {
        let composeViewController = MainViewControllerKt.WidgetViewController()
        // Pass any necessary data to your Compose view controller here
        return composeViewController
    }

    func updateUIViewController(_ uiViewController: UIViewController, context: Context) {
        // Update the view controller if needed
    }
}

struct WestVUEWidget: Widget {
    let kind: String = "WestVUEWidget"

    var body: some WidgetConfiguration {
        StaticConfiguration(kind: kind, provider: Provider()) { entry in
            if #available(iOS 17.0, *) {
                WestVUEWidgetEntryView(entry: entry)
                    .containerBackground(.fill.tertiary, for: .widget)
            } else {
                WestVUEWidgetEntryView(entry: entry)
                    .padding()
                    .background()
            }
        }
        .configurationDisplayName("My Widget")
        .description("This is an example widget.")
    }
}

#Preview(as: .systemSmall) {
    WestVUEWidget()
} timeline: {
    SimpleEntry(date: .now, currentClass: "Integrated Math 1", timeRemaining: "10 minutes")
    SimpleEntry(date: .now, currentClass: "Integrated Math 1", timeRemaining: "9 minutes")
}
