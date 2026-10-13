import Navbar from "../components/Navbar";
import Hero from "../components/Hero";
import Problem from "../components/Problem";
import FeatureBento from "../components/FeatureBento";
import HowItWorks from "../components/HowItWorks";
import RolesTabs from "../components/RolesTabs";
import Faq from "../components/Faq";
import FinalCta from "../components/FinalCta";
import Footer from "../components/Footer";
import "./LandingPage.css";

export default function LandingPage() {
  return (
    <>
      <Navbar />
      <main>
        <Hero />
        <Problem />
        <FeatureBento />
        <HowItWorks />
        <RolesTabs />
        <Faq />
        <FinalCta />
      </main>
      <Footer />
    </>
  );
}
